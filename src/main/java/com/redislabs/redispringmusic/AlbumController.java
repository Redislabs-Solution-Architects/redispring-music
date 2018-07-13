package com.redislabs.redispringmusic;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/albums")
public class AlbumController {

	@Autowired
	private AlbumRepository repository;

	@RequestMapping(method = RequestMethod.GET)
	public Iterable<Album> albums() {
		return repository.findAll();
	}

	@RequestMapping(method = RequestMethod.PUT)
	public Album add(@RequestBody @Valid Album album) {
		return repository.save(album);
	}

	@RequestMapping(method = RequestMethod.POST)
	public Album update(@RequestBody @Valid Album album) {
		return repository.save(album);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public Album getById(@PathVariable String id) {
		return repository.findById(id).orElse(null);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public void deleteById(@PathVariable String id) {
		repository.deleteById(id);
	}
}