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
public class ServerSideException extends Exception
{

    public ServerSideException(String errorMessage)
    {
        super(errorMessage);
    }
}
