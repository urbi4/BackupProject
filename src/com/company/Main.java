package com.company;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {

        String src,trg;
        Scanner sc = new Scanner(System.in);
        System.out.println("Zadejte vstupní složku (absolutní cestu): ");
        src = sc.nextLine();
        System.out.println("Zadejte výstupní složku (absolutní cestu): ");
        trg = sc.nextLine();

        BackupProvider backupProvider = new BackupProvider();
        backupProvider.doBackup(src,trg);
    }
}
