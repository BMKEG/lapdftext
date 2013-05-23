package edu.isi.bmkeg.lapdf.extraction.exceptions;

public class EncryptionException extends Exception{
	private String filename;

	public EncryptionException(String fileName) {
		this.filename = fileName;
	}

	@Override
	public void printStackTrace() {

		System.out.println(filename + " Permission Denied");
		super.printStackTrace();
	}
}
