package com.company;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class BackupProvider {

    public void doBackup(String src, String trg) throws IOException {

        File srcFile = new File(src);
        if(!srcFile.exists() || !srcFile.isDirectory())
            throw new IllegalArgumentException("Path" + src + " does not exist.");


        File trgFile = new File(trg);
        if(trgFile.exists() && !trgFile.isDirectory())
            throw new IllegalArgumentException("Path" + src + " is not a folder.");

        backupFolderFiles(srcFile,trgFile);
        backupFolderFolders(srcFile,trgFile);
    }

    private void backupFolderFolders(File srcFile, File trgFile) throws IOException {

        File[] srcFiles = listFileFiles(srcFile, f->f.isDirectory());

        for (File srcSubFile : srcFiles) {
            Path sourceSubPath = srcSubFile.toPath();
            Path targetSubPath = trgFile.toPath().resolve(srcSubFile.getName());

            backupFolderFiles(sourceSubPath.toFile(),targetSubPath.toFile());
        }
    }

    private void backupFolderFiles(File srcFile, File trgFile) throws IOException {
        if(!trgFile.exists()){
            trgFile.mkdirs();
        }

        File[] srcFiles = listFileFiles(srcFile,f->f.isFile());
        File[] trgFiles = listFileFiles(trgFile,f->f.isFile());

        if(srcFiles == null){
            throw new RuntimeException("Unable to read content of " + srcFile);
        }
        if(trgFiles == null){
            throw new RuntimeException("Unable to read content of " + trgFiles);
        }


        for (int srcIndex = 0; srcIndex<srcFiles.length; srcIndex++){
            System.out.print("Checking file " + srcFiles[srcIndex]);
            boolean isBackupNeeded = checkFileNeedsToBeBackedUp(srcFiles, trgFiles, srcIndex);
            System.out.print((isBackupNeeded ? " might be backed-up. \n" : " no backup necessary.\n"));
            if (isBackupNeeded){
                boolean isAlreadyBackedUp = checkFileAlreadyBackedUp(srcFiles[srcIndex],trgFiles);
                System.out.println(isAlreadyBackedUp ? " --is already backed-up!" : " will be backed-up");
                if(isAlreadyBackedUp) continue;
                Path srcFilePath = srcFiles[srcIndex].toPath();//C:\\Documents\\osu\\programko\\jaja.txt
                Path trgFilePath = trgFile.toPath().resolve(srcFilePath.getFileName());
                try {
                    java.nio.file.Files.copy(srcFilePath, trgFilePath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException(String.format("Failed to copy '%s' to '%s'.",srcFile.toString(),srcFile.toString()), e);
                }
            }
            if(!isBackupNeeded){
                Path srcFilePath = srcFiles[srcIndex].toPath();
                Path trgFilePath = trgFile.toPath().resolve(srcFilePath.getFileName());
                if(srcFilePath.getFileName().equals(trgFilePath.getFileName())){
                    if(srcIndex >= trgFiles.length) continue;
                    Path trgFilePathTemp = trgFiles[srcIndex].toPath();
                    Path trgFilePathCopy = managePaths(trgFilePathTemp);
//                    Path srcFilePathTemp = srcFiles[srcIndex].toPath();
//                    Path srcFilePathCopy = managePaths(srcFilePathTemp);
                    if(trgFilePathTemp.getFileName().toString().contains("2021")) continue;
                    try{
//                        java.nio.file.Files.copy(srcFilePath,srcFilePathCopy, StandardCopyOption.REPLACE_EXISTING);
//                        java.nio.file.Files.delete(srcFilePath);
                        java.nio.file.Files.copy(trgFilePath,trgFilePathCopy, StandardCopyOption.REPLACE_EXISTING);
                        java.nio.file.Files.delete(trgFilePath);
                    }catch (IOException e){
                        throw new RuntimeException(String.format("Failed to replace '%s' to '%s'.",srcFile.toString(),srcFile.toString()),e);
                    }
                    continue;
                }
            }
        }
    }

    private boolean checkFileAlreadyBackedUp(File srcFile, File[] trgFiles) {
        for (File trgFile : trgFiles) {
            String file = decomponent(trgFile);
            if(file.equals(srcFile.getName())){
                return true;
            }
        }
        return false;
    }

    private String decomponent(File srcFile) {
        String temp = srcFile.getName();
        String extension = "";
        if(!temp.contains(".")){
            extension = "";
        }
        else {
            extension = temp.substring(temp.indexOf('.'));
        }if(!temp.contains(",")) temp = srcFile.getName();
        else temp = temp.substring(0,temp.indexOf(','));
        return temp + extension;
    }

    private boolean checkFileNeedsToBeBackedUp(File[] srcFiles, File[] trgFiles, int srcIndex) {
        boolean isBackupNeeded;
        int trgIndex = tryGetTargetFileIndex(trgFiles, srcFiles[srcIndex]);
        isBackupNeeded = trgIndex<0;
        if(!isBackupNeeded){
            isBackupNeeded = srcFiles[srcIndex].length() != trgFiles[trgIndex].length();
            if(!isBackupNeeded){
                isBackupNeeded = !hasBothFilesTheSameContent(srcFiles[srcIndex], trgFiles[trgIndex]);
            }
        }
        return isBackupNeeded;
    }

    private Path managePaths(Path srcFilePath) {
        LocalDateTime ldt = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(",dd-MM-yyyy");
        String date = dtf.format(ldt);
        String extension, document, finalString = "";
        String path = srcFilePath.getFileName().toString();
        if(!path.contains(".")) {
            extension ="";
            document = path;
        }
        else{
            extension = path.substring(path.lastIndexOf('.'));
            document = path.substring(0,path.indexOf('.'));
        }
        if(document.contains("2021")){
            finalString = srcFilePath.getParent()+"\\"+document+extension;
            Path ret = Path.of(finalString);
            return ret;
        }
        finalString = srcFilePath.getParent()+"\\"+ document +date + extension;
        Path ret = Path.of(finalString);
        return ret;
    }

    private static File[] listFileFiles (File file, FileFilter fileFilter){
        File[] ret = file.listFiles(fileFilter);
        if(ret == null){
            throw new RuntimeException("Unable to read content of "+file);
        }
        return ret;

    }
    private boolean hasBothFilesTheSameContent(File a, File b) {
        assert a.length() == b.length();
        Boolean ret = null;

        try(BufferedInputStream bisa = new BufferedInputStream(new FileInputStream(a));
            BufferedInputStream bisb = new BufferedInputStream(new FileInputStream(b))){

            byte[] dataA = new byte[1024];
            byte[] dataB =new byte[1024];

            while(ret == null){

                int dataLenA = bisa.read(dataA);
                int dataLenB = bisb.read(dataB);
                assert dataLenA == dataLenB;


                if(dataLenA < 0){
                    ret = true;
                }else if(Arrays.equals(dataA, 0, dataLenA,dataB, 0,dataLenA) == false){
                    ret = false;
                }

            }
        }catch (IOException ex){
            throw new RuntimeException(String.format("Error comparing '%s' with '%s'.",a.toString(),b.toString()),ex);
        }

        assert ret != null;
        return ret;
    }

    private int tryGetTargetFileIndex(File[] trgFiles, File srcFile) {
        int ret = -1;

        String srcFileName = srcFile.getName();
        for (int i = 0; i < trgFiles.length;i++){
            if(trgFiles[i].getName().equals(srcFileName)){
                ret = i;
                break;
            }
        }
        return ret;
    }
}
