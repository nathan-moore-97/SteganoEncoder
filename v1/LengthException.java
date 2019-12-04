
public class LengthException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public LengthException(String errMessage) {
		System.err.println(errMessage);
	}
}
