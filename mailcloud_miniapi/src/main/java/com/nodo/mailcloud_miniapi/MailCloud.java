package com.nodo.mailcloud_miniapi;

import android.util.Log;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.google.gson.JsonObject;
import com.nodo.mailcloud_miniapi.json.Dispatche;
import com.nodo.mailcloud_miniapi.json.FileInfo;
import com.nodo.mailcloud_miniapi.json.FolderInfo;
import com.nodo.mailcloud_miniapi.json.InfoWithBody;
import com.nodo.mailcloud_miniapi.json.ShareUrlInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.CookieJar;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

class MailCloud {


    private static final String ROOT_DIR = "/";
    private final boolean async;
    private RetroCloudApi retroCloudApi;
    private Tokenizer tokenizer;
    private OkHttpClient httpClient;
    private boolean cancelled;

    public MailCloud(String login, String password, boolean async) throws IOException, MailCloudException {
        this.async = async;
        if (login == null || password == null || login.equals("") || password.equals(""))
            throw new IOException("Empty login or password");
        CookieJar cookieJar = new PersistentCookieJar(
                new SetCookieCache(),
                new CookieContainer());
        httpClient = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();
        retroCloudApi = new Retrofit.Builder()
                .baseUrl(RequestsUtil.baseUrl)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(RetroCloudApi.class);
        tokenizer = new Tokenizer(login, password, httpClient);
    }

    public FolderInfo getFolderInfo(String path_in_cloud) throws MailCloudException {
        if (path_in_cloud == null || path_in_cloud.equals("")) path_in_cloud = ROOT_DIR;
        InfoWithBody infoWithBody = getInfoBody(path_in_cloud, false);
        return infoWithBody.deserializeToFolderInfo();
    }

    public FileInfo getFileInfo(String path_in_cloud) throws MailCloudException {
        if (path_in_cloud == null || path_in_cloud.equals("")) path_in_cloud = ROOT_DIR;
        InfoWithBody infoWithBody = getInfoBody(path_in_cloud, true);
        return infoWithBody.deserializeToFileInfo();
    }

    private InfoWithBody getInfoBody(String path_in_cloud, boolean is_file) throws MailCloudException {
        try {
            Response<InfoWithBody> response = is_file ? retroCloudApi.getFileInfo(path_in_cloud, tokenizer.getToken()).execute() : retroCloudApi.getFolderInfo(path_in_cloud, tokenizer.getToken()).execute();
            MailCloudException.checkExceptionForCode(response.code(), (is_file ? "File" : "Folder") + " info for " + path_in_cloud + " was not collect");
            return response.body();
        } catch (IOException e) {
            throw new MailCloudException(
                    MailCloudException.Type.ConnectionError,
                    (is_file ? "File" : "Folder") + " info for " + path_in_cloud + " was not collect: " + e);
        }
    }

    public void downloadFiles(ProgressCollector collector, String cloud_dirpath, String dirpath_on_phone) throws Exception {
        validateNotNull(collector, "collector");
        validateNotNull(cloud_dirpath, "cloud_dirpath");
        validateNotNull(dirpath_on_phone, "dirpath_on_phone");
        checkFileRequires(new File(dirpath_on_phone), false);
        try {
            ShareUrlInfo get = updateLink(true);
            int count_of_use = get.getCount();

            FolderInfo main_folderInfo = getFolderInfo(cloud_dirpath);
            if (collector.numOfFilesRequired())
                collector.setNum_of_files(getCountOfFilesForDownloading(main_folderInfo));

            if (main_folderInfo.getFilesCount() > 0) {
                for (FileInfo fileInfo : main_folderInfo.getFiles()) {
                    if (cancelled) {
                        cancelled = false;
                        return;
                    }
                    downloadFile(collector, get.getUrl(), fileInfo.getFullPath(), dirpath_on_phone + "/" + fileInfo.getName());
                    count_of_use--;
                    if (count_of_use == 0) {
                        get = updateLink(true);
                        count_of_use = get.getCount();
                    }
                }

            }
            if (main_folderInfo.getFoldersCount() > 0) {
                File current_dir;
                for (FolderInfo folderInfo : main_folderInfo.getFolders()) {
                    if (cancelled) {
                        cancelled = false;
                        return;
                    }
                    current_dir = new File(dirpath_on_phone + "/" + folderInfo.getName());
                    if (!current_dir.exists()) {
                        if (current_dir.mkdir())
                            downloadFiles(collector, folderInfo.getFullpath(), dirpath_on_phone + "/" + folderInfo.getName());
                        else
                            throw new Exception("Dir was not created. Check rights for writing");
                    } else
                        downloadFiles(collector, folderInfo.getFullpath(), dirpath_on_phone + "/" + folderInfo.getName());

                }
            }
        } catch (MailCloudException e) {
            collector.sendException(e);
        }
    }

    private int getCountOfFilesForDownloading(FolderInfo folderInfo) throws MailCloudException {
        int res = folderInfo.getFilesCount();
        if (folderInfo.getFoldersCount() > 0)
            for (FolderInfo info : folderInfo.getFolders()) {
                if (cancelled)
                    break;
                res += getCountOfFilesForDownloading(getFolderInfo(info.getFullpath()));
            }
        return res;
    }

    /**
     * @throws IOException if dirpath_on_phone not exist or not directory
     */
    public void downloadFiles(ProgressCollector collector, String cloud_dir, List<String> filenames_in_cloud, String dirpath_on_phone) throws IOException {
        validateNotNull(collector, "collector");
        validateNotNull(cloud_dir, "cloud_dir");
        validateNotNull(filenames_in_cloud, "filenames_in_cloud");
        validateNotNull(dirpath_on_phone, "dirpath_on_phone");
        checkFileRequires(new File(dirpath_on_phone), false);

        if (collector.numOfFilesRequired())
            collector.setNum_of_files(filenames_in_cloud.size());
        try {
            ShareUrlInfo get = updateLink(true);
            int count_of_load = get.getCount();
            for (String filename : filenames_in_cloud) {
                if (cancelled) {
                    cancelled = false;
                    return;
                }
                if (count_of_load == 0) {
                    get = updateLink(true);
                    count_of_load = get.getCount();
                }
                downloadFile(collector, get.getUrl(), cloud_dir + "/" + filename, dirpath_on_phone + "/" + filename);
                count_of_load--;
            }
        } catch (MailCloudException e) {
            collector.sendException(e);
        }
    }

    private ShareUrlInfo updateLink(boolean isDownload) throws MailCloudException {
        try {
            Response<Dispatche> dispatcher = retroCloudApi.getDispatcherUrls(tokenizer.getToken()).execute();
            MailCloudException.checkExceptionForCode(dispatcher.code(), (isDownload ? "Down" : "Up") + "load failed. URL was not received");
            return isDownload ? dispatcher.body().urlGet() : dispatcher.body().urlUpload();
        } catch (IOException e) {
            throw new MailCloudException(MailCloudException.Type.ConnectionError, (isDownload ? "Down" : "Up") + "load failed. URL was not received: " + e);
        }
    }

    public void downloadFile(ProgressCollector collector, final String fullpath_in_cloud, final String fullpath_in_phone) {
        validateNotNull(collector, "collector");
        validateNotNull(fullpath_in_cloud, "fullpath_in_cloud");
        validateNotNull(fullpath_in_phone, "fullpath_in_phone");
        ShareUrlInfo get = null;
        try {
            get = updateLink(true);
        } catch (MailCloudException e) {
            collector.sendException(e);
        }
        downloadFile(collector, get.getUrl(), fullpath_in_cloud, fullpath_in_phone);
    }

    private void downloadFile(final ProgressCollector collector, String urlGet, final String fullpath_in_cloud, final String fullpath_in_phone) {
        Call<ResponseBody> loadfile = retroCloudApi.download(urlGet + fullpath_in_cloud);
        if (async) {
            loadfile.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    downloadResponseProc(collector, response, fullpath_in_phone);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    collector.sendConnectionError(t.getMessage());
                }
            });
        } else {
            try {
                downloadResponseProc(collector, loadfile.execute(), fullpath_in_phone);
            } catch (IOException e) {
                collector.sendConnectionError(e.getMessage());
            }
        }
    }

    private void downloadResponseProc(ProgressCollector collector, Response<ResponseBody> response, String fullpath_in_phone) {
        if (response.isSuccessful()) {
            File file = new File(fullpath_in_phone);
            try {
                if (!file.exists()) {
                    if (!new File(fullpath_in_phone).createNewFile()) {
//                        collector.sendUnknownError("Download failed for " + response.body().string() + ". Check rights for writing.");
                        collector.sendUnknownError("Download failed for " + fullpath_in_phone + ". Check rights for writing.");
                    }
                } else {
                    Log.w("download", "File already exist");
                }
                new FileOutputStream(fullpath_in_phone).write(response.body().bytes());
                collector.incrementCurrentNum();
            } catch (IOException e) {
                collector.sendUnknownError(e.getMessage());
            }
        } else {
            collector.sendException(response.code(), "Download failed for " + fullpath_in_phone);
        }
    }

    /**
     * Add to FolderInfo other FolderInfos with lists of files and folders recursively
     */
    public FolderInfo getFoldersTree(String cloud_dir) throws MailCloudException {
        FolderInfo folderInfo = getFolderInfo(cloud_dir);
        return getFoldersTree(folderInfo);
    }

    /**
     * Add to FolderInfo other FolderInfos with lists of files and folders recursively
     */
    public FolderInfo getFoldersTree(FolderInfo folderInfo) throws MailCloudException {
        validateNotNull(folderInfo, "folderInfo");
        List<FolderInfo> folderInfos = new ArrayList<>();
        for (FolderInfo folder : folderInfo.getFolders()) {
            if (cancelled)
                break;
            folderInfos.add(getFoldersTree(getFolderInfo(folder.getFullpath())));
        }
        folderInfo.updateFolders(folderInfos);
        return folderInfo;
    }

    /**
     * The simplest synchronization without comparing the contents of files
     *
     * @throws IOException if directories for sync not dirs or not exist
     */
    public void simpleSync(ProgressCollector collector, String cloud_dir_fullpath, String phone_dir_fullpath, SyncParams params) throws IOException {
        validateNotNull(collector, "collector");
        validateNotNull(cloud_dir_fullpath, "cloud_dir_fullpath");
        validateNotNull(phone_dir_fullpath, "phone_dir_fullpath");
        validateNotNull(params, "params");
        simpleSync(collector, cloud_dir_fullpath, phone_dir_fullpath, params, true);
    }

    private void simpleSync(ProgressCollector collector, String cloud_dir_fullpath, String phone_dir_fullpath, SyncParams params, boolean first_call) throws IOException {
        collector.setDynamicMax();
        FolderInfo main_folderInfo = null;
        try {
            main_folderInfo = getFolderInfo(cloud_dir_fullpath);
        } catch (MailCloudException e) {
            if (first_call || e.getType() != MailCloudException.Type.FileNotFound)
                collector.sendException(e);
        }
        List<String> cloud_filenames = new ArrayList<>(),
                cloud_foldernames = new ArrayList<>(),
                phone_filenames = new ArrayList<>(),
                phone_foldernames = new ArrayList<>();


        if (main_folderInfo != null) {
            if (main_folderInfo.getFilesCount() > 0)
                cloud_filenames = main_folderInfo.getFilenames();
            if (main_folderInfo.getFoldersCount() > 0)
                cloud_foldernames = main_folderInfo.getFoldernames();
        }

        File[] phone_files = new File(phone_dir_fullpath).listFiles();
        if (phone_files != null && phone_files.length > 0) {
            for (File file : phone_files) {
                if (file.isFile())
                    phone_filenames.add(file.getName());
                else
                    phone_foldernames.add(file.getName());
            }
        }

        List<String> cloud_original_filenames = null,
                phone_original_filenames = null,
                cloud_original_foldernames = null,
                phone_original_foldernames = null;

        if (params.loadToPhone() || params.delFromCloud()) {
            cloud_original_filenames = new ArrayList<>(cloud_filenames);
            cloud_original_filenames.removeAll(phone_filenames);
            cloud_original_foldernames = new ArrayList<>(cloud_foldernames);
            cloud_original_foldernames.removeAll(phone_foldernames);
        }
        if (params.loadToCloud() || params.delFromPhone()) {
            phone_original_filenames = new ArrayList<>(phone_filenames);
            phone_original_filenames.removeAll(cloud_filenames);
            phone_original_foldernames = new ArrayList<>(phone_foldernames);
            phone_original_foldernames.removeAll(cloud_foldernames);
        }

        if (params.loadToPhone()) {
            if (cloud_original_foldernames != null && cloud_original_foldernames.size() > 0)
                for (String cloud_original_foldername : cloud_original_foldernames) {
                    File file = new File(phone_dir_fullpath + "/" + cloud_original_foldername);
                    if (!file.exists())
                        if (!file.mkdir())
                            throw new IOException("Dir " + file.getAbsolutePath() + " can't be created. Check rights for writing");
                }
            if (cloud_original_filenames != null && cloud_original_filenames.size() > 0) {
                downloadFiles(collector, cloud_dir_fullpath, cloud_original_filenames, phone_dir_fullpath);
                collector.addToMaxNum(cloud_original_filenames.size());
            }
        }
        if (params.delFromPhone()) {
            if (phone_original_filenames != null && phone_original_filenames.size() > 0) {
                collector.addToMaxNum(phone_original_filenames.size());
                for (String filename : phone_original_filenames)
                    if (!new File(phone_dir_fullpath + "/" + filename).delete())
                        collector.sendUnknownError("File " + phone_dir_fullpath + "/" + filename + " can't be deleted");
            }
            if (phone_original_foldernames != null && phone_original_foldernames.size() > 0)
                for (String foldername : phone_original_foldernames) {
                    try {
                        deleteDir(phone_dir_fullpath + "/" + foldername);
                    } catch (MailCloudException e) {
                        collector.sendException(e);
                    }
                }
        }

        if (params.loadToCloud()) {
            if (phone_original_filenames != null && phone_original_filenames.size() > 0) {
                collector.addToMaxNum(phone_original_filenames.size());
                uploadFiles(collector, phone_dir_fullpath, phone_original_filenames, cloud_dir_fullpath);
            }
            if (phone_original_foldernames != null && phone_original_foldernames.size() > 0)
                createFolders(collector, cloud_dir_fullpath, phone_original_filenames);
        }
        if (params.delFromCloud()) {
            if (cloud_original_foldernames != null && cloud_original_foldernames.size() > 0)
                deleteFilesOrFolders(collector, cloud_dir_fullpath, cloud_original_foldernames);
            if (cloud_original_filenames != null && cloud_original_filenames.size() > 0) {
                collector.addToMaxNum(cloud_original_filenames.size());
                deleteFilesOrFolders(collector, cloud_dir_fullpath, cloud_original_filenames);
            }
        }

        for (File foldername : new File(phone_dir_fullpath).listFiles(File::isDirectory)) {
            simpleSync(collector, cloud_dir_fullpath + "/" + foldername.getName(), foldername.getAbsolutePath(), params, false);
        }
    }

    private void deleteDir(String dir) throws MailCloudException {
        String[] files = new File(dir).list();
        if (files != null && files.length > 0)
            for (String file : files) {
                File f = new File(dir + "/" + file);
                if (f.isDirectory()) {
                    deleteDir(f.getAbsolutePath());
                }
                if (!f.delete())
                    throw new MailCloudException(MailCloudException.Type.Unknown, "File " + dir + "/" + file + "can't be deleted");
            }
    }

    /**
     * If file or folder not exist then ignore it
     */
    public void deleteFileOrFolder(String filepath_in_cloud) throws MailCloudException {
        validateNotNull(filepath_in_cloud, "filepath_in_cloud");
        try {
            Response<JsonObject> response = retroCloudApi.deleteFileOrFolder(filepath_in_cloud, tokenizer.getToken()).execute();
            MailCloudException.checkExceptionForCode(response.code(), "Deletion failed for " + filepath_in_cloud);
        } catch (IOException e) {
            throw new MailCloudException(MailCloudException.Type.ConnectionError, e.getMessage());
        }
    }

    /**
     * If file or folder not exist then ignore it
     */
    public void deleteFilesOrFolders(ProgressCollector collector, List<String> filepathes_in_cloud) {
        validateNotNull(collector, "collector");
        validateNotNull(filepathes_in_cloud, "filepathes_in_cloud");
        deleteFilesOrFolders(collector, "", filepathes_in_cloud);
    }

    /**
     * If file or folder not exist then ignore it
     */
    public void deleteFilesOrFolders(final ProgressCollector collector, String cloud_dir, List<String> filenames_in_cloud) {
        validateNotNull(cloud_dir, "cloud_dir");
        validateNotNull(collector, "collector");
        validateNotNull(filenames_in_cloud, "filenames_in_cloud");
        if (collector.numOfFilesRequired())
            collector.setNum_of_files(filenames_in_cloud.size());
        if (async) {
            for (String filenames : filenames_in_cloud) {
                if (cancelled) {
                    cancelled = false;
                    return;
                }
                retroCloudApi.deleteFileOrFolder(
                        cloud_dir + "/" + filenames,
                        tokenizer.getToken())
                        .enqueue(new Callback<JsonObject>() {
                            @Override
                            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                if (response.isSuccessful())
                                    collector.incrementCurrentNum();
                                else
                                    collector.sendException(response.code(), "Deletion failed");

                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                collector.sendConnectionError("Deletion failed: " + t);
                            }
                        });
            }
        } else {
            Response<JsonObject> response;
            for (String filename : filenames_in_cloud) {
                if (cancelled) {
                    cancelled = false;
                    return;
                }
                try {
                    response = retroCloudApi.deleteFileOrFolder(
                            cloud_dir + "/" + filename,
                            tokenizer.getToken()).execute();
                    if (response.isSuccessful())
                        collector.incrementCurrentNum();
                    else
                        collector.sendException(response.code(), "Deletion failed for " + cloud_dir + "/" + filename);
                } catch (IOException e) {
                    collector.sendConnectionError("Deletion failed for " + cloud_dir + "/" + filename + ": " + e);
                }
            }
        }
    }

    public void createFolder(String fullpath_in_cloud) throws MailCloudException {
        validateNotNull(fullpath_in_cloud, "fullpath_in_cloud");
        try {
            MailCloudException.checkExceptionForCode(retroCloudApi.addFolder(fullpath_in_cloud, tokenizer.getToken()).execute().code(), "Creation folder failed for " + fullpath_in_cloud);
        } catch (IOException e) {
            throw new MailCloudException(MailCloudException.Type.ConnectionError, "Creation folder failed for " + fullpath_in_cloud);
        } catch (MailCloudException e) {
            if (e.getType() != MailCloudException.Type.FileNotFound)
                throw e;
        }
    }

    private void createFolder(ProgressCollector collector, String fullpath_in_cloud) throws MailCloudException {
        try {
            MailCloudException.checkExceptionForCode(retroCloudApi.addFolder(fullpath_in_cloud, tokenizer.getToken()).execute().code(), "Creation folder failed for " + fullpath_in_cloud);
            collector.incrementCurrentNum();
        } catch (IOException e) {
            throw new MailCloudException(MailCloudException.Type.ConnectionError, "Creation folder failed for " + fullpath_in_cloud);
        } catch (MailCloudException e) {
            if (e.getType() != MailCloudException.Type.FileNotFound)
                throw e;
        }
    }

    public void createFolders(ProgressCollector collector, List<String> filepathes_in_cloud) {
        validateNotNull(collector, "collector");
        validateNotNull(filepathes_in_cloud, "filepathes_in_cloud");
        createFolders(collector, null, filepathes_in_cloud);
    }

    public void createFolders(final ProgressCollector collector, String cloud_dir, List<String> filenames) {
        validateNotNull(cloud_dir, "cloud_dir");
        validateNotNull(collector, "collector");
        validateNotNull(filenames, "filenames");
        if (collector.numOfFilesRequired())
            collector.setNum_of_files(filenames.size());
        if (async) {
            for (String filename : filenames) {
                if (cancelled) {
                    cancelled = false;
                    return;
                }
                retroCloudApi.addFolder(
                        cloud_dir == null ? filename : cloud_dir + "/" + filename,
                        tokenizer.getToken())
                        .enqueue(new Callback<JsonObject>() {
                            @Override
                            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                if (response.isSuccessful())
                                    collector.incrementCurrentNum();
                                else
                                    collector.sendException(response.code(), "Folder creation failed");

                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                collector.sendConnectionError("Folder creation failed: " + t);
                            }
                        });
            }
        } else {
            for (String filename : filenames) {
                if (cancelled) {
                    cancelled = false;
                    return;
                }
                try {
                    createFolder(collector, cloud_dir + "/" + filename);
                } catch (MailCloudException e) {
                    collector.sendException(e);
                }

            }
        }
    }

    /**
     * Can stop work with next tasks and tasks that already work
     *
     * @param with_processing stop tasks that already work (download/upload predominantly)
     */
    public void stopRequests(boolean with_processing) {
        cancelled = true;
        if (with_processing)
            httpClient.dispatcher().cancelAll();
    }

    private void uploadFile(final ProgressCollector collector, String uploadUrl, final File file, final String fullpath_in_cloud) {
        if (file.isFile()) {
            final MultipartBody.Part body = MultipartBody.Part.createFormData(
                    "file",
                    file.getName(),
                    RequestBody.create(MediaType.parse("multipart/form-data"), file));
            if (async) {
                retroCloudApi.startUpload(uploadUrl, body).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            try {
                                String hash = response.body().string().split(";")[0];
                                retroCloudApi.endUpload(fullpath_in_cloud, hash, "rename", tokenizer.getToken(), body.body().contentLength()).enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        if (response.isSuccessful())
                                            collector.incrementCurrentNum();
                                        else
                                            collector.sendException(response.code(), "Upload failed");
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        collector.sendConnectionError(t.getMessage());
                                    }
                                });
                            } catch (IOException e) {
                                collector.sendConnectionError(e.getMessage());
                            }
                        } else
                            collector.sendException(response.code(), "Upload failed");
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        collector.sendConnectionError(t.getMessage());
                    }
                });
            } else {
                try {
                    Response<ResponseBody> response = retroCloudApi.startUpload(uploadUrl, body).execute();
                    if (response.isSuccessful()) {
                        String hash = response.body().string().split(";")[0];
                        response = retroCloudApi.endUpload(
                                fullpath_in_cloud,
                                hash,
                                "rename",
                                tokenizer.getToken(),
                                body.body().contentLength()
                        ).execute();
                        if (response.isSuccessful())
                            collector.incrementCurrentNum();
                        else
                            collector.sendException(response.code(), "Upload failed for " + file.getAbsolutePath()
                            );
                    } else {
                        collector.sendException(response.code(), "Upload failed for " + file.getAbsolutePath());
                    }
                } catch (IOException e) {
                    collector.sendUnknownError(e.getMessage());
                }
            }
        }
    }

    /**
     * Get hash to file from Cloud. Full file loading to service required.
     *
     * @throws MailCloudException
     * @throws IOException        if file not exist or directory
     */
    public String callFileHash(File file) throws MailCloudException, IOException {
        validateNotNull(file, "file");
        checkFileRequires(file, true);
        try {
            ShareUrlInfo upload = updateLink(false);
            MultipartBody.Part body = MultipartBody.Part.createFormData(
                    "file",
                    file.getName(),
                    RequestBody.create(MediaType.parse("multipart/form-data"), file));
            Response<ResponseBody> response = retroCloudApi.startUpload(upload.getUrl(), body).execute();

            MailCloudException.checkExceptionForCode(response.code(), "Get hash failed");
            return response.body().string().split(";")[0];
        } catch (IOException e) {
            throw new MailCloudException(MailCloudException.Type.ConnectionError, e.getMessage());
        }
    }

    private void checkFileRequires(File file, boolean file_need) throws IOException {
        if (file.exists()) {
            if (file_need) {
                if (!file.isFile())
                    throw new IOException(file.getAbsolutePath() + " is directory");
            } else {
                if (!file.isDirectory())
                    throw new IOException(file.getAbsolutePath() + " is file");
            }
        } else
            throw new IOException(file.getAbsolutePath() + " not exist");
    }

    /**
     * Upload file to server. If dirs for file not exist it will create dirs.
     *
     * @throws IOException if file not exist or not directory
     */
    public void uploadFile(ProgressCollector collector, File phone_file, String fullpath_in_cloud) throws IOException {
        validateNotNull(collector, "collector");
        validateNotNull(phone_file, "phone_file");
        validateNotNull(fullpath_in_cloud, "fullpath_in_cloud");
        checkFileRequires(phone_file, true);
        ShareUrlInfo upload = null;
        try {
            upload = updateLink(false);
        } catch (MailCloudException e) {
            collector.sendException(e);
        }
        uploadFile(collector, upload.getUrl(), phone_file, fullpath_in_cloud);
    }

    private int getCountOfFilesForUpload(File[] files) {
        int res = 0;
        if (files != null && files.length != 0)
            for (File file : files) {
                if (file.isFile())
                    res++;
                else
                    res += getCountOfFilesForUpload(file.listFiles());
            }
        return res;
    }

    /**
     * Upload files to server. If dirs for file not exist it will create dirs.
     *
     * @throws IOException if file from phone_filenames not exist or not directory
     */
    public void uploadFiles(ProgressCollector collector, String phone_dir, List<String> phone_filenames, String folderpath_in_cloud) throws IOException {
        validateNotNull(collector, "collector");
        validateNotNull(phone_filenames, "phone_filenames");
        validateNotNull(folderpath_in_cloud, "folderpath_in_cloud");
        validateNotNull(phone_dir, "phone_dir");

        File[] files = new File[phone_filenames.size()];
        for (int i = 0; i < files.length; i++) {
            files[i] = new File(phone_dir + "/" + phone_filenames.get(i));
        }
        uploadFiles(collector, files, folderpath_in_cloud);

    }

    /**
     * Upload files to server. If dirs for file not exist it will create dirs.
     *
     * @throws IOException if file not exist or not directory
     */
    public void uploadFiles(ProgressCollector collector, File[] phone_files, String folderpath_in_cloud) throws IOException {
        validateNotNull(collector, "collector");
        validateNotNull(phone_files, "phone_files");
        validateNotNull(folderpath_in_cloud, "folderpath_in_cloud");
        if (collector.numOfFilesRequired())
            collector.setNum_of_files(getCountOfFilesForUpload(phone_files));
        try {
            ShareUrlInfo upload = updateLink(false);
            int num_of_links = upload.getCount();
            for (File file : phone_files) {
                if (cancelled) {
                    cancelled = false;
                    return;
                }
                if (num_of_links == 0) {
                    upload = updateLink(false);
                    num_of_links = upload.getCount();
                }
                if (file.isFile())
                    uploadFile(collector, upload.getUrl(), file, folderpath_in_cloud + "/" + file.getName());
                else {
                    createFolder(folderpath_in_cloud + "/" + file.getName());
                    uploadFiles(collector, file.getAbsolutePath(), folderpath_in_cloud + "/" + file.getName());
                }
                num_of_links--;
            }
        } catch (MailCloudException e) {
            collector.sendException(e);
        }
    }

    /**
     * Upload files to server. If dirs for file not exist it will create dirs.
     *
     * @throws IOException if file not exist or not directory
     */
    public void uploadFiles(ProgressCollector collector, String dirpath_in_phone, String dirpath_in_cloud) throws IOException {
        validateNotNull(collector, "collector");
        validateNotNull(dirpath_in_cloud, "dirpath_in_cloud");
        validateNotNull(dirpath_in_phone, "dirpath_in_phone");
        File dir = new File(dirpath_in_phone);
        checkFileRequires(dir, false);
        if (collector.numOfFilesRequired())
            collector.setNum_of_files(getCountOfFilesForUpload(dir.listFiles()));
        File[] files = dir.listFiles();
        if (files != null && files.length != 0) {
            uploadFiles(collector, files, dirpath_in_cloud);
        }
    }

    private void validateNotNull(Object o, String name) {
        if (o == null)
            throw new IllegalArgumentException(name + " can't be null");
    }

    /**
     * Can help with AccessDenied Exception
     */
    public void recreateToken() throws MailCloudException {
        tokenizer.recreateToken();
    }
}