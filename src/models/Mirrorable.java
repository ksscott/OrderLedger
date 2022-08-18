package models;

public interface Mirrorable {
	/** Mirror from top to bottom. Default implementation does nothing. */
	public default void mirror() {}
}
