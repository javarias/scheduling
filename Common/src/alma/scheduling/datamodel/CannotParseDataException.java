package alma.scheduling.datamodel;

public class CannotParseDataException extends Exception {

    private static final long serialVersionUID = 4399567874855790838L;

    /**
     * 
     * @param Path of the file that cannot be parsed
     * @param ex The exception causing the problem
     */
    public CannotParseDataException(String file, Exception ex){
        super("Cannot parse the file: "+ file, ex);
    }
}
