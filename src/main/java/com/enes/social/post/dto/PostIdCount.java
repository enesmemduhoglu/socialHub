package com.enes.social.post.dto;

/**
 * "Gönderi id → adet" toplu sayım sorguları için projeksiyon
 * (beğeni ve yorum sayıları grup bazında tek sorguda çekilir).
 */
public interface PostIdCount {

    Long getPostId();

    long getCnt();
}
