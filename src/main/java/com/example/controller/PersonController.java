package com.example.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.Person;
import com.example.service.PersonService;

@CrossOrigin
@RestController
@RequestMapping("/persons")
public class PersonController {

	private final PersonService ps;

	public PersonController(PersonService ps) {
		this.ps = ps;
	}

	@RequestMapping("/all")
	public List<Person> getAll() {
		return ps.getAll();
	}

	@RequestMapping("{id}")
	public Person getPerson(@PathVariable("id") String id) {
		return ps.getPerson(id);
	}
}

