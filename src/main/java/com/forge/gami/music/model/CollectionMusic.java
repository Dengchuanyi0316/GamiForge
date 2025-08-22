package com.forge.gami.music.model;

public class CollectionMusic {
    private Integer collectionId;  // 歌单ID
    private Integer musicId;       // 音频ID

    public Integer getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Integer collectionId) {
        this.collectionId = collectionId;
    }

    public Integer getMusicId() {
        return musicId;
    }

    public void setMusicId(Integer musicId) {
        this.musicId = musicId;
    }

    @Override
    public String toString() {
        return "CollectionMusic{" +
                "collectionId=" + collectionId +
                ", musicId=" + musicId +
                '}';
    }
}
