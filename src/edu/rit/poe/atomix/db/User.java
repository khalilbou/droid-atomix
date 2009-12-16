/*
 * User.java
 *
 * Version:
 *      $Id$
 *
 * Copyright (c) 2009 Peter O. Erickson
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without major
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, and/or distribute copies of, but
 * under no circumstances sublicense and/or sell, the Software,
 * and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package edu.rit.poe.atomix.db;

/**
 *
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public class User {
    
    public static final String ID = "user_id";
    
    public static final String USERNAME_KEY = "username";
    
    public static final String SAVED_GAME_ID = "saved_game_id";
    
    private long id;
    
    private String username;
    
    private Game savedGame;
    
    /**
     * Constructs a new <tt>User</tt>.
     */
    public User() {
    }

    public long getId() {
        return id;
    }

    public void setId( long id ) {
        this.id = id;
    }

    public Game getSavedGame() {
        return savedGame;
    }

    public void setSavedGame( Game savedGame ) {
        this.savedGame = savedGame;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername( String username ) {
        this.username = username;
    }
    
} // User
