package com.zebra.jamesswinton.scantocloudwedge.networking;

import com.zebra.jamesswinton.scantocloudwedge.data.ScanEvent;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface EndpointApi {

  // Get All Printers
  @POST("{endpoint}")
  Call<ScanEvent> sendScanEventToCloud(
          @Path("endpoint") String endpoint,
          @Body ScanEvent scanEvent
  );

}
