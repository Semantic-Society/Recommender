package uni.rwth.neolog.recommeder.rest;

import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@Path("/recommend")
public class RecommendVocabulary {
	
	JSONObject jsonMain = new JSONObject();
	
	@GET 
	@Path("{query}")
    @Produces({ "MediaType.APPLICATION_JSON", "MediaType.APPLICATION_XML" })
	@Consumes({"application/x-www-form-urlencoded"})
	@SuppressWarnings("unchecked")
	public Response recommendService(@PathParam("query") String query) {
			
			ResponseBuilder arg9999;
				
			arg9999 = Response.status(200);

			return arg9999.entity(JSONObject.toJSONString(this.jsonMain).toString())
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").allow(new String[]{"OPTIONS"})
					.build();
		}
	
}
