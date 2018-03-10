package uni.rwth.neolog.recommeder.rest;

import java.io.IOException;
import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import uni.rwth.neolog.recommeder.model.QueryVirtuoso;

@Path("/recommend")
public class RecommendVocabulary {

	@GET
	@Path("{query}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	// @Consumes({"application/x-www-form-urlencoded"})
	public Response recommendService(@PathParam("query") String query) {

		JSONObject jsonMain = new JSONObject();
		JSONArray jsonArray = new JSONArray();

		ResponseBuilder arg9999;

		QueryVirtuoso queryVirtuoso = new QueryVirtuoso();

		ArrayList<String> vRecommendList = queryVirtuoso.getVirtuosoRecommend(query);

		for (int j = 0; j < vRecommendList.size(); j++) {
			jsonArray.add(vRecommendList.get(j));
		}

		jsonMain.put("list", jsonArray);
		System.out.println(jsonMain);

		arg9999 = Response.status(200);

		// local requests
		DcatConnection dcat = new DcatConnection();
		dcat.search(query);

		DctermsConnection dcterms = new DctermsConnection();
		dcterms.search(query);

		// LOV request
		RequestLov requestLov = new RequestLov();
		try {
			requestLov.request(query);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Bioportal request
		Request requestBioP = new Request();
		try {
			requestBioP.request("", query);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return arg9999.entity(JSONObject.toJSONString(jsonMain).toString()).header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").allow(new String[] { "OPTIONS" })
				.build();
	}

}
