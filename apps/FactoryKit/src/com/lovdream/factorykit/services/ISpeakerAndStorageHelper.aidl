package com.lovdream.factorykit.services;

interface ISpeakerAndStorageHelper{
    void playMusic(String path);
    void stopMusic();
    boolean isSdMounted();
    boolean copyTestToSd(String toPath);
    boolean createTestFile(String path);
    int getCopyProgress(String path);
    String getSDPath();
}