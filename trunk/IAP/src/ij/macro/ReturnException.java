package ij.macro;

/** This runtime exceptions is thrown when return is invoked in a user-defined function. */
class ReturnException extends RuntimeException {
	double value;
	String str;
	Variable[] array;
	
	ReturnException() {
	}
	
	// ReturnException(double value, String str, Variable[] array) {
	// this.value = value;
	// this.str = str;
	// this.array = array;
	// }
	
}
