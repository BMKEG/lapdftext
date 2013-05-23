package edu.isi.bmkeg.lapdf.extraction.exceptions;

public class AccessException extends Exception  {

	private String filename;

	public AccessException(String fileName) {
		this.filename = fileName;
	}

	@Override
	public void printStackTrace() {

		System.out.println(filename + " Permission Denied");
		super.printStackTrace();
	}

}
