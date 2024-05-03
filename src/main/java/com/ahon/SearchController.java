package com.ahon;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Servlet implementation class SearchController
 */
@WebServlet("/search")
public class SearchController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SearchController() {
		super();
		// TODO Auto-generated constructor stub
	}

	private HttpResponse<String> fetch(String url, String method) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
		HttpResponse<String> response = client.sendAsync(request, BodyHandlers.ofString()).join();

		return response;
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {

			Double latitude = getLocation(request.getParameter("text")).getDouble("latitude");
			Double longitude = getLocation(request.getParameter("text")).getDouble("longitude");

			HttpResponse<String> res = fetch("https://api.open-meteo.com/v1/forecast?" + "latitude=" + latitude
					+ "&longitude=" + longitude
					+ "&hourly=temperature_2m,relativehumidity_2m,weathercode,windspeed_10m&timezone=America%2FLos_Angeles",
					"get");

			JSONObject json = new JSONObject(res.body()).getJSONObject("hourly");
			String temperature = (String) json.getJSONArray("temperature_2m").get(0).toString();
			String humidity = (String) json.getJSONArray("relativehumidity_2m").get(0).toString();
			String windspeed = (String) json.getJSONArray("windspeed_10m").get(0).toString();
			request.setAttribute("temperature", temperature);
			request.setAttribute("humidity", humidity);
			request.setAttribute("windspeed", windspeed);
			RequestDispatcher d =request.getRequestDispatcher("/show.jsp");
			d.forward(request, response);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private JSONObject getLocation(String city) throws IOException, InterruptedException, JSONException {
		HttpResponse<String> res = fetch(
				"https://geocoding-api.open-meteo.com/v1/search?name=" + city + "&count=10&language=en&format=json",
				"get");

		JSONObject json = new JSONObject(res.body());
//		System.out.println(json.toString());
//		
		JSONObject location = json.getJSONArray("results").getJSONObject(0);
		return location;

	}

}
