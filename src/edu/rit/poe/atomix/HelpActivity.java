/*
 * HelpActivity.java
 *
 * Version:
 *      $Id$
 *
 * Copyright (c) 2010 Peter O. Erickson
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

package edu.rit.poe.atomix;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * An activito view the DroidAtomix help file.
 * 
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public class HelpActivity extends Activity {
    
    /** The absolute path of the help file. */
    public static final String HELP_FILE =
            "file:///android_asset/help/index.html";
    
    /**
     * Called when the activity is first created.
     *
     * @param   icicle  the bundle of saved data
     */
    @Override
    public void onCreate( Bundle icicle ) {
        super.onCreate( icicle );
        super.setTitle( R.string.help_title );
        super.setContentView( R.layout.help );
        
        // set the display page of the webview
        WebView webView = ( WebView )super.findViewById( R.id.help_webkit );
        webView.loadUrl( HELP_FILE );
    }
    
} // HelpActivity
