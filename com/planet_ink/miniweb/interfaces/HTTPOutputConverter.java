package com.planet_ink.miniweb.interfaces;

import java.io.File;
import java.nio.ByteBuffer;

import com.planet_ink.miniweb.http.HTTPException;
import com.planet_ink.miniweb.http.HTTPStatus;
import com.planet_ink.miniweb.util.MiniWebConfig;

/*
Copyright 2012-2014 Bo Zimmerman

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

/**
 * Interface for any class that can convert an HTML output buffer
 * for the web server to send to clients.  Includes some helpful
 * constants that are often used in common http requests.  Works
 * by calling convertOutput to convert the input and calling
 * generateOutput to get the new output.
 * @author Bo Zimmerman
 *
 */
public interface HTTPOutputConverter
{

	/**
	 * Standard method for converting an intput buffer for writing to
	 * the client.   The position and limit of the bytebuffer must
	 * already be set for reading the content.
	 * Call generateOutput() to get the new output.
	 * @param config the http configuration
	 * @param request the http request bring processed
	 * @param status the status of the request (so far)
	 * @param buffer the input buffer
	 * @param pageFile the file whose data is being converted
	 * @return the output buffer
	 * @throws HTTPException
	 */
	public ByteBuffer convertOutput(MiniWebConfig config, HTTPRequest request, File pageFile, HTTPStatus status, ByteBuffer buffer) throws HTTPException;
}
