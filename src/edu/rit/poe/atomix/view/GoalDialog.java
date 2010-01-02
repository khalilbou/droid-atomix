/*
 * GoalDialog.java
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

package edu.rit.poe.atomix.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

/**
 *
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public class GoalDialog {
    
    
    public static Dialog getDialog( Activity activity ) {
        AlertDialog.Builder builder = null;
        
        Dialog d = builder.create();
        
        return d;
    }
    
    public static void prepareDialog( Activity activity ) {
        
    }
    
    // @todo make a separate class that can be called statically for drawing
    // the solution panel into the game window
    public static class SolutionView extends View {
        
        public SolutionView( Context context ) {
            super( context );
            
            
            
        }
        
        @Override
        public void onDraw( Canvas canvas ) {
            
            
            
        }
        
        public static void drawGoal( Canvas canvas ) {
            
        }
        
    } // SolutionView
    
} // GoalDialog
