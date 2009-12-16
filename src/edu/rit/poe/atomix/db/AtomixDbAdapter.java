/*
 * AtomixDbAdapter.java
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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.Point;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * 
 * 
 * @author  Peter O. Erickson
 * 
 * @version $Id$
 */
public class AtomixDbAdapter {
    
    private static final int DATABASE_VERSION = 1;
    
    public static final String DATABASE_NAME = "Atomix";
    
    public static final String USER_TABLE_NAME = "USER";
    
    public static final String GAME_TABLE_NAME = "GAME";
    
    public static final String ATOM_TABLE_NAME = "ATOM";
    
    private static final String DATABASE_CREATE =
            "create table notes (_id integer primary key autoincrement, "
            + "title text not null, body text not null);";
    
    private SQLiteDatabase database;
    
    private DatabaseHelper databaseHelper;
    
    private final Context context;
    
    /**
     * 
     * 
     * @param   context     the <tt>Context</tt> within which to work
     */
    public AtomixDbAdapter( Context context ) {
        this.context = context;
    }
    
    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public AtomixDbAdapter open() throws SQLException {
        databaseHelper = new DatabaseHelper( context, DATABASE_NAME, null,
                DATABASE_VERSION );
        database = databaseHelper.getWritableDatabase();
        
        return this;
    }
    
    public void close() {
        databaseHelper.close();
    }
    
    private ContentValues getValues( User user ) {
        ContentValues values = new ContentValues();
        values.put( User.USERNAME_KEY, user.getUsername() );
        
        // only save the game ID if it's valid
        Game game = user.getSavedGame();
        if ( ( game != null ) && ( game.getId() != -1 ) ) {
            values.put( User.SAVED_GAME_ID, game.getId() );
        }
        
        return values;
    }
    
    public void insert( User user ) {
        ContentValues values = getValues( user );
        
        // insert into the DB and set the user ID
        long userId = database.insert( USER_TABLE_NAME, null, values );
        user.setId( userId );
    }
    
    
    public void update( User user ) {
        ContentValues values = getValues( user );
        
        String where = User.ID + "=" + user.getId();
        if ( database.update( USER_TABLE_NAME, values, where, null ) == 0 ) {
            throw new SQLException( "Error saving user: "
                    + user.getUsername() );
        }
    }
    
    private ContentValues getValues( Game game ) {
        ContentValues values = new ContentValues();
        values.put( Game.CREATED_KEY, game.getCreated().getTimeInMillis() );
        values.put( Game.SAVED_KEY, game.getSaved().getTimeInMillis() );
        values.put( Game.LEVEL_KEY, game.getLevel() );
        values.put( Game.MOVES_KEY, game.getMoves() );
        values.put( Game.SECONDS_KEY, game.getSeconds() );
        values.put( Game.FINISHED_KEY, game.isFinished() );
        
        // only save the user ID if it's valid
        User user  = game.getUser();
        if ( ( user != null ) && ( user.getId() != -1 ) ) {
            values.put( Game.USER_ID, user.getId() );
        }
        
        return values;
    }
    
    public void insert( Game game ) {
        ContentValues values = getValues( game );
        
        // insert into the DB and set the game ID
        long gameId = database.insert( GAME_TABLE_NAME, null, values );
        game.setId( gameId );
        
        // insert the atom locations also
        Map<Integer, Point> atoms = game.getAtoms();
        for ( Map.Entry<Integer, Point> atom : atoms.entrySet() ) {
            values.clear();
            // populate the values of each atom
            values.put( Game.ATOM_GAME_ID, game.getId() );
            values.put( Game.ATOM_MARKER_KEY, atom.getKey() );
            values.put( Game.ATOM_X, atom.getValue().x );
            values.put( Game.ATOM_Y, atom.getValue().y );
            
            // insert each atom, disregard the row ID
            database.insert( ATOM_TABLE_NAME, null, values );
        }
    }
    
    public void update( Game game ) {
        ContentValues values = getValues( game );
        
        // update the main Game information
        String where = Game.ID + "=" + game.getId();
        if ( database.update( GAME_TABLE_NAME, values, where, null ) == 0 ) {
            throw new SQLException(
                    "Error saving game (ID: " + game.getId() + ")." );
        }
        
        // update atom locations in the game
        Map<Integer, Point> atoms = game.getAtoms();
        for ( Map.Entry<Integer, Point> atom : atoms.entrySet() ) {
            values.clear();
            // populate the values of each atom
            values.put( Game.ATOM_X, atom.getValue().x );
            values.put( Game.ATOM_Y, atom.getValue().y );
            
            where = Game.ATOM_GAME_ID + "=" + game.getId() + " AND "
                    + Game.ATOM_MARKER_KEY + "=" + atom.getKey();
            if ( database.update(
                    ATOM_TABLE_NAME, values, where, null ) == 0 ) {
                throw new SQLException( "Error saving atom." );
            }
        }
    }
    
    public List<User> getSavedUsers() {
        List<User> users = new ArrayList<User>();
        Cursor cursor = database.query( USER_TABLE_NAME,
                new String[] { User.ID, User.USERNAME_KEY, User.SAVED_GAME_ID },
                null, null, null, null, null);
        
        String where = null;
        Cursor gameCursor = null;
        for ( int i = 0; i < cursor.getCount(); i++ ) {
            cursor.moveToNext();
            
            // create a new user
            User user = new User();
            user.setId( cursor.getLong( 0 ) );
            user.setUsername( cursor.getString( 1 ) );
            
            // load the respective saved game (column 2 = saved_game_id)
            where = Game.ID + "=" + cursor.getLong( 2 );
            gameCursor = database.query( GAME_TABLE_NAME,
                    new String[] { Game.ID, Game.CREATED_KEY, Game.SAVED_KEY,
                    Game.LEVEL_KEY, Game.MOVES_KEY, Game.SECONDS_KEY,
                    Game.FINISHED_KEY }, where, null, null, null, null );
            
            // create a new game
            Game game = new Game();
            game.setId( gameCursor.getLong( 0 ) );
            
            Calendar created = Calendar.getInstance();
            created.setTimeInMillis( gameCursor.getLong( 1 ) );
            game.setCreated( created );
        }
        
        return users;
    }
    
    /*
    public boolean deleteNote(long rowId) {
        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public Cursor fetchAllNotes() {
        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
                KEY_BODY}, null, null, null, null, null);
    }
    
    
    public Cursor fetchNote(long rowId) throws SQLException {
        Cursor mCursor =
                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                        KEY_TITLE, KEY_BODY}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    */
    
    /**
     * 
     * 
     * @author  Peter O. Erickson
     * 
     * @version $Id$
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        
        /**
         * 
         * @param   context     
         * @param   name        
         * @param   factory     
         * @param   version     
         */
        public DatabaseHelper( Context context, String name,
                CursorFactory factory, int version ) {
            super( context, name, factory, version );
        }
        
        /**
         * 
         * 
         * @param   db  
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            //db.execSQL( DATABASE_CREATE );
            
            // @todo create USER table
            
            // @todo create GAME table
            
            // @todo create ATOM table
        }
        
        /**
         * 
         * 
         * @param   db          
         * @param   oldVersion  
         * @param   newVersion  
         */
        @Override
        public void onUpgrade( SQLiteDatabase db, int oldVersion,
                int newVersion ) {
            // @todo implement an on-upgrade strategy?
            //db.execSQL( "DROP TABLE IF EXISTS notes" );
            //onCreate( db );
        }
        
    } // DatabaseHelper
    
} // AtomixDBAdapter
