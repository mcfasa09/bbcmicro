package com.fiskur.bbcmicro;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

public class ByteRequest extends Request<byte[]> {
	
	 private final Listener<byte[]> mListener;

	public ByteRequest(int method, String url, Listener<byte[]> listener, ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
	}

	@Override
	protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
		byte[] responseData = response.data;
		return Response.success(responseData, HttpHeaderParser.parseCacheHeaders(response));
	}

	@Override
	protected void deliverResponse(byte[] response) {
		mListener.onResponse(response);
	}

}
