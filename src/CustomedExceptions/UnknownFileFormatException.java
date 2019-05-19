/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CustomedExceptions;

/**
 *
 * @author Dorian
 */
public class UnknownFileFormatException extends Exception
{

    public UnknownFileFormatException(String errorMessage)
    {
        super(errorMessage);
    }

}
