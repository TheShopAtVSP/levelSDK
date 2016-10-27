package com.theshopatvsp.levelandroidsdk.ble.helper;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by andrco on 1/28/16.
 */
public class FileHelper {
    private static final String TAG = FileHelper.class.getSimpleName();
    private static final ReentrantReadWriteLock writeLock = new ReentrantReadWriteLock();

    public static final String STEPS_FILE = "com.theshopatvsp.level.steps.file";
    public static final String BATTERY_FILE = "com.theshopatvsp.level.batteryReport.file";
    public static final String MOTION_DATA_FILE = "com.theshopatvsp.level.motion.data.file";
    public static final String LAST_USER_LOCATION_FILE = "com.theshopatvsp.level.last.user.location.file";

    public static boolean saveObjectToFile(Context activityContext, String filename, Object obj) {

        writeLock.writeLock().lock();

        boolean success = true;

        try {
            FileOutputStream fos = activityContext.openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(obj);
            os.close();
        } catch (IOException e) {
            Log.v(TAG, e.toString());
            success = false;
        } finally {
            writeLock.writeLock().unlock();
        }

        return success;
    }

    public static boolean writeToFile(Context activityContext, String filename, String stuff, boolean append) {

        writeLock.writeLock().lock();

        boolean success = true;
        int mode = Context.MODE_PRIVATE;

        if( append )
            mode = Context.MODE_PRIVATE | Context.MODE_APPEND;

        try {
            FileOutputStream fos = activityContext.openFileOutput(filename, mode);
            stuff += "\n";
            Log.e(TAG, "stuff in writeToFile is " + stuff);
            fos.write(stuff.getBytes());
            // fos.write("\n".getBytes());
            fos.close();
        } catch (IOException e) {
            Log.v(TAG, e.toString());
            success = false;
        } finally {
            writeLock.writeLock().unlock();
        }

        return success;
    }

    public static void delete(Context activityContext, String filename) {
        activityContext.deleteFile(filename);
    }

    public static String readAndClearFile(Context activityContext, String filename) {

        writeLock.writeLock().lock();

        StringBuilder buffer = new StringBuilder();

        try {
            InputStream fis = activityContext.openFileInput(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;

            while( (line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            reader.close();

            activityContext.deleteFile(filename);
        } catch (IOException e) {
            Log.v(TAG, e.toString());
        } catch (Exception e) {

        } finally {
            writeLock.writeLock().unlock();
        }

        return buffer.toString();
    }

    public static String read(Context activityContext, String filename) {

        writeLock.writeLock().lock();

        StringBuilder buffer = new StringBuilder();

        try {
            InputStream fis = activityContext.openFileInput(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;

            while( (line = reader.readLine()) != null) {
                buffer.append(line);
            }

            reader.close();
        } catch (IOException e) {
            Log.v(TAG, e.toString());
        } finally {
            writeLock.writeLock().unlock();
        }

        return buffer.toString();
    }

    public static Object getObjectFromFile(Context activityContext, String filename) {
        Log.v(TAG, "Reading: " + filename);
        Object obj = null;

        writeLock.writeLock().lock();

        ObjectInputStream is = null;
        FileInputStream fis = null;
        try {
            fis = activityContext.openFileInput(filename);
            is = new ObjectInputStream(fis);
            obj = is.readObject();
        } catch (FileNotFoundException fe) {
            //do nothing file just wasn't written yet
        } catch (InvalidClassException e) {
            Log.v(TAG, e.toString());
        } catch (IOException e) {
            Log.v(TAG, e.toString());
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            Log.v(TAG, e.toString());
            throw new RuntimeException(e);
        } finally {
            try {
                if (fis != null)
                    fis.close();

                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                Log.v(TAG, e.toString());
                throw new RuntimeException(e);
            }

            writeLock.writeLock().unlock();
        }

        Log.v(TAG, "Exiting: " + filename);

        return obj;
    }

    public static Object loadJsonStringFromFileToObject(Context context, String filename, Class classSource) {

        try {

            String json = (String)FileHelper.getObjectFromFile(context, filename);

            if (json != null) {

                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                try {
                    return mapper.readValue(json, classSource);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception ex) {
            Log.v(TAG, "Error Loading User Profile from File: " + ex.getMessage());
        }

        return null;

    }

    public static String mapObjectToJsonString(Object obj) {
        String json = null;
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        try {
            json = ow.writeValueAsString(obj);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return json;

    }
}
