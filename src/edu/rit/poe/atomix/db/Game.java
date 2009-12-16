/*
 * Game.java
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

import android.graphics.Point;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public class Game {
    
    public static final String ID = "game_id";
    
    public static final String USER_ID = "user_id";
    
    public static final String CREATED_KEY = "created";
    
    public static final String SAVED_KEY = "saved";
    
    public static final String LEVEL_KEY = "level";
    
    public static final String MOVES_KEY = "moves";
    
    public static final String SECONDS_KEY = "seconds";
    
    public static final String FINISHED_KEY = "finished";
    
    public static final String ATOM_GAME_ID = "game_id";
    
    public static final String ATOM_MARKER_KEY = "atom_marker_id";
    
    public static final String ATOM_X = "x";
    
    public static final String ATOM_Y = "y";
    
    private long id;
    
    private User user;
    
    private Calendar created;
    
    private Calendar saved;
    
    private int level;
    
    private int moves;
    
    private int seconds;
    
    private boolean finished;
    
    private Map<Integer, Point> atoms;
    
    /**
     * Constructs a new <tt>Game</tt>.
     */
    public Game() {
        atoms = new HashMap<Integer, Point>();
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
    
    public Map<Integer, Point> getAtoms() {
        return atoms;
    }
    
} // Game
