/*
 * GameDatabase.java
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

package edu.rit.poe.atomix.game;

import android.content.Context;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public final class GameDatabase {
    
    public static final String GAME_FILENAME = "saved.obj";
    
    private static Data data;
    
    public static boolean isLoaded() {
        return ( data != null );
    }
    
    public static void load( Context context ) throws IOException {
        // try loading the database object
        try {
            FileInputStream fis = context.openFileInput( GAME_FILENAME );
            ObjectInputStream ois = new ObjectInputStream( fis );
            
            // load the data object from the file
            data = ( Data )ois.readObject();
            
        } catch ( Exception e ) {
            // if there is any error loading the file, make a new game state and
            // save it to the filesystem
            
            data = new Data();
            
            try {
                save( context );
            } catch ( IOException e2 ) {
                // if there is any problem SAVING the new game, fatally fail
                throw new IOException( "Error loading/creating new game." );
            }
        }
    }
    
    public static void save( Context context ) throws IOException {
        if ( data == null ) {
            throw new IllegalStateException( "Database not loaded." );
        }
        
        // save the "last-played" date of the active game
        data.last.lastPlayed = Calendar.getInstance();
        
        try {
            FileOutputStream fos = context.openFileOutput( GAME_FILENAME,
                    Context.MODE_PRIVATE );
            ObjectOutputStream oos = new ObjectOutputStream( fos );
            
            // save the data object to the file
            oos.writeObject( data );
            
        } catch ( Exception e ) {
            // if there is any problem SAVING the new game, fatally fail
            throw new IOException( "Error saving new game." );
        }
    }
    
    public static GameState newGame( String user ) {
        if ( data == null ) {
            throw new IllegalStateException( "Database not loaded." );
        }
        
        GameState game = new GameState( user );
        data.active.add( game );
        data.last = game;
        
        return game;
    }
    
    public static List<GameState> getActive() {
        if ( data == null ) {
            throw new IllegalStateException( "Database not loaded." );
        }
        
        // sort the data in recently-played order
        Collections.sort( data.active );
        
        return data.active;
    }
    
    public static final class Data implements Serializable {
        
        private GameState last;
        
        private List<GameState> active;
        
        private List<GameState> finished;
        
        private Data() {
            last = null;
            active = new ArrayList<GameState>();
            finished = new ArrayList<GameState>();
        }
        
    } // Manager
    
} // GameDatabase
