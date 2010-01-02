/*
 * User.java
 *
 * Version:
 *      $Id$
 *
 * Copyright (c) 2009 Peter O. Erickson
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
