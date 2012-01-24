package unit_test_support;

/**
 * Use this annotation to ensure that value used by the random property setter is different from NULL.
 * 
 * @author klukas
 */
public @interface TestValueRequired {
	
	String value();
	
}
