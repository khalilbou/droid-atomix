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

import java.io.Serializable;

/**
 * This class represents a single user that is playing the Atomix game.  Each
 * user is linked to a single currently active <tt>Game</tt> and <tt>n</tt>
 * finished <tt>Game</tt>s to represents the statistics of a completed level.
 * 
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public class User implements Serializable {
    
    public static final String ID_KEY = "id";
    
    public static final String USERNAME_KEY = "username";
    
    public static final String SAVED_GAME_ID_KEY = "saved_game_id";
    
    private long id;
    
    private String username;
    
    private Game currentGame;
    
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

    public Game getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame( Game currentGame ) {
        this.currentGame = currentGame;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername( String username ) {
        this.username = username;
    }
    
} // User
