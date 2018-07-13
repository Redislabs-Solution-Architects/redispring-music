package com.redislabs.redispringmusic;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import lombok.Data;

@Data
@RedisHash("album")
public class Album {
	
	@Id
	String id;
	String title;
	@Indexed
	String artist;
	String year;
	String genre;
	String cover;
	
}
