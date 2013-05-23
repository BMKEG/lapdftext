package edu.isi.bmkeg.lapdf.extraction.exceptions;

public class EmptyPDFException extends Exception {
	private String filename;

	public EmptyPDFException(String fileName) {
		this.filename = fileName;
	}

	@Override
	public void printStackTrace() {

		System.out.println(filename + " is empty");
		super.printStackTrace();
	}
}
