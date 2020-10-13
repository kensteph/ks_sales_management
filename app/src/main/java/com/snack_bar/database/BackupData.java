package com.snack_bar.database;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.snack_bar.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;

public class BackupData {
    // url for database /data/data/com.snack_bar
    private final String dataPath = "//data//com.snack_bar//databases//";
    //DATABASE NAME
    private String DATABASE_NAME="dbKSSM";
    //TABLE TO BACKUP
    private String TABLE_TO_BACKUP= "empreintes";
    // name of main data
    private final String dataName = DATABASE_NAME;
    // data main
    private final String data = dataPath + dataName;
    // name of temp data
    private final String dataTempName = DATABASE_NAME + "_temp";
    // temp data for copy data from sd then copy data temp into main data
    private final String dataTemp = dataPath + dataTempName;
    //Context
    private Context context;
    // folder on sd to backup data
    private  String folderSD="";

    public BackupData(Context context) {
        this.context = context;
        this.folderSD = context.getExternalFilesDir(null).getAbsolutePath()+"/Steph";
    }

    DatabaseHelper db = new DatabaseHelper(context);

    // create folder if it not exist
    private void createFolder() {
        File sd = new File(folderSD);
        if (!sd.exists()) {
            sd.mkdir();
            System.out.println("create folder"+folderSD);
        } else {
            System.out.println("exits : "+folderSD);
        }
    }

    /**
     * Copy database to sd card
     * name of file = database name + time when copy
     * When finish, we call onFinishExport method to send notify for activity
     */
    public void exportToSD() {

        String error = null;
        try {

            createFolder();

            File sd = new File(folderSD);

            if (sd.canWrite()) {

                SimpleDateFormat formatTime = new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss");
                String backupDBPath = dataName+"-bckUp"; //+ "_" + formatTime.format(new Date());

                File currentDB = new File(Environment.getDataDirectory(), data);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                } else {
                    System.out.println("db not exist");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            error = "Error backup";
        }
        onBackupListener.onFinishExport(error);
    }

    /**
     * import data from file backup on sd card
     * we must create a temp database for copy file on sd card to it.
     * Then we copy all row of temp database into main database.
     * It will keep struct of curren database not change when struct backup database is old
     *
     * @param fileNameOnSD name of file database backup on sd card
     */
    public void importData(String fileNameOnSD) {

        File sd = new File(folderSD);

        // create temp database
        SQLiteDatabase dbBackup = context.openOrCreateDatabase(dataTempName, SQLiteDatabase.CREATE_IF_NECESSARY, null);

        String error = null;

        if (sd.canWrite()) {

            File currentDB = new File(Environment.getDataDirectory(), dataTemp);
            File backupDB = new File(sd, fileNameOnSD);

            if (currentDB.exists()) {
                FileChannel src;
                try {
                    src = new FileInputStream(backupDB).getChannel();
                    FileChannel dst = new FileOutputStream(currentDB)
                            .getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    error = "Error load file";
                } catch (IOException e) {
                    error = "Error import";
                }
            }
        }
        /**
         *when copy old database into temp database success
         * we copy all row of table into main database
         */

        if (error == null) {
            new CopyDataAsyncTask(dbBackup).execute();
        } else {
            onBackupListener.onFinishImport(error);
        }
    }

    /**
     * show dialog for select backup database before import database
     * if user select yes, we will export curren database
     * then show dialog to select old database to import
     * else we onoly show dialog to select old database to import
     */
    public void importFromSD() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Backup").setIcon(R.mipmap.ic_launcher)
                .setMessage("Voulez-vous ?");
        builder.setPositiveButton("OUI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showDialogListFile(folderSD);
            }
        });
        builder.setNegativeButton("NON", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showDialogListFile(folderSD);
                exportToSD();
            }
        });
        builder.show();
    }

    /**
     * show dialog list all backup file on sd card
     * @param forderPath folder conatain backup file
     */
    private void showDialogListFile(String forderPath) {
        createFolder();

        File forder = new File(forderPath);
        File[] listFile = forder.listFiles();

        final String[] listFileName = new String[listFile.length];
        for (int i = 0, j = listFile.length - 1; i < listFile.length; i++, j--) {
            listFileName[j] = listFile[i].getName();
        }

        if (listFileName.length > 0) {
                    importData(listFileName[0]);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
            builder.setTitle("Title").setIcon(R.mipmap.ic_launcher)
                    .setMessage("MEsasage..."+forderPath);
            builder.show();
        }
    }

    /**
     * AsyncTask for copy data
     */
    class CopyDataAsyncTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progress = new ProgressDialog(context);
        SQLiteDatabase db;

        public CopyDataAsyncTask(SQLiteDatabase dbBackup) {
            this.db = dbBackup;
        }

        /**
         * will call first
         */

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            progress.setMessage("Importing...");
            progress.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            copyData(db);
            return null;
        }

        /**
         * end process
         */
        @Override
        protected void onPostExecute(Void error) {
            // TODO Auto-generated method stub
            super.onPostExecute(error);
            if (progress.isShowing()) {
                progress.dismiss();
            }
            onBackupListener.onFinishImport(null);
        }
    }

    /**
     * copy all row of temp database into main database
     * @param dbBackup
     */
    private void copyData(SQLiteDatabase dbBackup) {
        //DatabaseHelper db = new DatabaseHelper(context);
        Log.d("BCKUP","CLEAR FINGER PRINTS TABLE");
        /** copy all row of subject table */
        Cursor cursor = dbBackup.query(true, TABLE_TO_BACKUP, null, null, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            Log.d("BCKUP","FOR EACH  FINGER PRINTS ADD IT");
            cursor.moveToNext();
        }
        cursor.close();
        Log.d("BCKUP","DELETE THE TEMPORARY DB");
        context.deleteDatabase(dataTempName);
    }


    private OnBackupListener onBackupListener;

    public void setOnBackupListener(OnBackupListener onBackupListener) {
        this.onBackupListener = onBackupListener;
    }

    public interface OnBackupListener {
        public void onFinishExport(String error);

        public void onFinishImport(String error);
    }
}
