/*
 * Game.java
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

import edu.rit.poe.atomix.util.Point;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the game state of a single level.  This class is a
 * transfer object of the <tt>GAME</tt> database table.  One game exists per
 * <tt>User</tt> in the active (<tt>finished = false</tt>) state, while
 * <tt>n</tt> instances of finished games may exist per user, to represents the
 * game state of a level that has been completed by a user.
 * 
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public class Game implements Serializable {
    
    public static final String ID_KEY = "id";
    
    public static final String USER_ID_KEY = "user_id";
    
    public static final String CREATED_KEY = "created";
    
    public static final String SAVED_KEY = "saved";
    
    public static final String LEVEL_KEY = "level";
    
    public static final String MOVES_KEY = "moves";
    
    public static final String SECONDS_KEY = "seconds";
    
    public static final String FINISHED_KEY = "finished";
    
    public static final String ATOM_GAME_ID_KEY = "game_id";
    
    public static final String ATOM_MARKER_KEY = "atom_marker";
    
    public static final String ATOM_X_KEY = "x";
    
    public static final String ATOM_Y_KEY = "y";
    
    private long id;
    
    private User user;
    
    private Calendar created;
    
    private Calendar saved;
    
    private int level;
    
    private int moves;
    
    private int seconds;
    
    private boolean finished;
    
    private Map<Short, Point> atoms;
    
    /**
     * Constructs a new <tt>Game</tt>.
     */
    public Game() {
        atoms = new HashMap<Short, Point>();
    }

    public Calendar getCreated() {
        return created;
    }

    public void setCreated( Calendar created ) {
        this.created = created;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished( boolean finished ) {
        this.finished = finished;
    }

    public long getId() {
        return id;
    }

    public void setId( long id ) {
        this.id = id;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel( int level ) {
        this.level = level;
    }

    public int getMoves() {
        return moves;
    }

    public void setMoves( int moves ) {
        this.moves = moves;
    }

    public Calendar getSaved() {
        return saved;
    }

    public void setSaved( Calendar saved ) {
        this.saved = saved;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds( int seconds ) {
        this.seconds = seconds;
    }

    public User getUser() {
        return user;
    }

    public void setUser( User user ) {
        this.user = user;
    }
    
    public Map<Short, Point> getAtoms() {
        return atoms;
    }
    
} // Game
