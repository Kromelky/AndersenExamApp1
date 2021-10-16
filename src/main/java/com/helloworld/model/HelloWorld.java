package com.helloworld.model;

public class HelloWorld {

	public HelloWorld() {
		
	}
	public HelloWorld(Integer id, String message) {
		super();
		this.id = id;
		this.message = message;
	}
	
	private Integer id;
	private String message;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}


	@Override
	public String toString() {
		return "Message [id=" + id + ", firstName=" + message + "]";
	}
}
