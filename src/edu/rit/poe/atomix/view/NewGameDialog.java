/*
 * NewGameDialog.java
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

package edu.rit.poe.atomix.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import edu.rit.poe.atomix.AtomicActivity;
import edu.rit.poe.atomix.R;
import edu.rit.poe.atomix.game.GameDatabase;
import edu.rit.poe.atomix.game.GameState;

/**
 *
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public class NewGameDialog extends Dialog {
    
    private Activity context;
    
    private EditText editText;
    
    /**
     * Constructs a new <tt>NewGameDialog</tt>.
     * 
     * @param   context     the application context
     */
    public NewGameDialog( Activity context ) {
        super( context );
        this.context = context;
    }
    
    /**
     * Creates this dialog and sets the view.
     * 
     * @param   icicle  the bundle of frozen state, if any
     */
    @Override
    public void onCreate( Bundle icicle ) {
        super.onCreate( icicle );
        
        super.setContentView( R.layout.game_dialog );
        
        editText = ( EditText )super.findViewById( R.id.new_name );

        // set the start game button
        Button startGame = ( Button )super.findViewById( R.id.play_button );
        startGame.setOnClickListener( new View.OnClickListener() {
            public void onClick( View view ) {
                // create a new game
                GameState.setCurrent(
                        GameDatabase.newGame( editText.toString() ) );

                Intent intent = new Intent( context, AtomicActivity.class );
                context.startActivity( intent );
                
                // do not return to the menu activity with the back button
                context.finish();
            }
        } );
    }
    
    /**
     * Clears the name text box in this dialog.
     */
    public void clearText() {
        if ( editText != null ) {
            editText.setText( "" );
        }
    }
    
} // NewGameDialog
