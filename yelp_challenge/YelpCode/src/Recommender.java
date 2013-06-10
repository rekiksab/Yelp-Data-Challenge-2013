import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Recommender {

	HashMap<String, HashMap<String, Long>> ratings;

	public static final int NB_USERS    = 45981;
	public static final int NB_BUSINESS = 11537;


	public Recommender()
	{
		this.ratings = new HashMap<String, HashMap<String, Long>>();
	}
	
	/**
	 * Load the json data and store in ratings map
	 *
	 * @param filename  the json file to be analyzed
	 * @return  		void
	 */
	public void loadRatings(String fileName) throws FileNotFoundException, ParseException
	{
		JSONParser parser = new JSONParser();
		Scanner in = new Scanner(new File(fileName));
		while(in.hasNextLine())
		{
			String content = in.nextLine().replace("\\.", "");
			Object obj = parser.parse(content);
			JSONObject jsonObject = (JSONObject) obj;

			// read the json object
			String user_id = (String) jsonObject.get("user_id");
			String business_id = (String) jsonObject.get("business_id");
			Long stars = (Long)jsonObject.get("stars");

			// store in the structure if stars > threshold
			if (ratings.containsKey(user_id))
			{
				ratings.get(user_id).put(business_id, stars);
			}
			else
			{
				HashMap<String,Long> userGrades = new HashMap<String, Long>();
				userGrades.put(business_id, stars);
				ratings.put(user_id, userGrades);
			}
		}
	}

	/**
	 * Compute a similarity score between two users
	 *
	 * @param user1  the first user
	 * @param user2  the second user
	 * @return 		 the similarity score between the two users
	 */
	public double getSimilarity(String user1, String user2)
	{
		HashMap<String, Long> grades1 = this.ratings.get(user1);
		HashMap<String, Long> grades2 = this.ratings.get(user2);

		// norm of the user vectors
		double norm1 = getNorm(grades1.values());
		double norm2 = getNorm(grades2.values());
		// numerator : produit scalaire
		double product = 0;
		for (String business : grades1.keySet())
		{
			if (grades2.containsKey(business))
			{
				product += (grades1.get(business) * grades2.get(business));
			}
		}		
		double similarity = product / (norm1 * norm2);
		return similarity;
	}

	/**
	 * Normalization for a user, used when computing similarity
	 *
	 * @param vector  a vector of reviews for a particular user
	 * @return 		  normalization for a particular user
	 */
	private double getNorm(Collection<Long> vector)
	{
		double norm = 0;
		for (Long i : vector)
		{
			norm += i*i;
		}
		return Math.sqrt(norm);
	}
	
	/**
	 * Retrieve the top 20 users that have the highest similarity to a particular user
	 *
	 * @param userSims  A map of similarity scores, for a single user, to every other user
	 * @return 			The top 20 users with the highest similarity score
	 */
	private ArrayList<String> getTopUsers(HashMap<String, Double> userSims)
	{
		ArrayList<String> topUsers = new ArrayList<String>();
		
		// retrieve top 20 users with highest similarity score
		for (int i=0; i < 20; i++)
		{
			Map.Entry<String, Double> maxEntry = null;
			for (Map.Entry<String, Double> entry : userSims.entrySet())
			{
			    if ((maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) && !topUsers.contains(entry.getKey()))
			    {
			        maxEntry = entry;
			    }
			}
			topUsers.add(maxEntry.getKey());
		}
		return topUsers;
	}
	
	/**
	 * Find a business recommendation for a user. This is computed by finding the business with
	 * the highest average rating between the top 20 users with the highest similarity score.
	 * 
	 * TODO: normalization
	 *
	 * @param topUsers  the top 20 users with the highest similarity scores
	 * @return 		 	a business recommendation for a particular user
	 */
	private String getFinalRec(ArrayList<String> topUsers)
	{
		HashMap<String, Long> topBus = new HashMap<String, Long>();
		
		for (String user : topUsers)
		{
			HashMap<String, Long> topUserReviews = ratings.get(user);
			for (Map.Entry<String, Long> entry : topUserReviews.entrySet())
			{
				if (topBus.containsKey(entry.getKey()))
				{
					String key = entry.getKey();
					Long value = topBus.get(key) + entry.getValue();
					topBus.put(key, value);
				}
				else
				{
					topBus.put(entry.getKey(), entry.getValue());
				}
			}
		}
		
		Map.Entry<String, Long> maxEntry = null;
		for (Map.Entry<String, Long> entry : topBus.entrySet())
		{
		    if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
		    {
		        maxEntry = entry;
		    }
		}
		return maxEntry.getKey();
	}
	
	/**
	 * For all users, find the similarity against all other users
	 *
	 * @param user_id  The user to find similarities against
	 * @return 		   void
	 */
	public void compareToAllUsers(String user_id)
	{
		int num = 1;
		HashMap<String, Double> userSims = new HashMap<String, Double>();
		
		for (String user : ratings.keySet())
		{
			if (!user.equals(user_id))
			{
				double sim = getSimilarity(user_id, user);
				userSims.put(user, sim);
			}
		}
		
		ArrayList<String> topUsers = getTopUsers(userSims);
		System.out.println("User: " + user_id);
		for (String u : topUsers)
		{
			System.out.println(num +" " + u);
			num++;
		}
		String finalRec = getFinalRec(topUsers);
		System.out.println("Recommendation: " + finalRec);
		System.out.print("\n");
	}
	public static void main(String[] argv) throws FileNotFoundException, ParseException
	{
		System.out.println("I loooooove you!");
		Recommender yelpRecommender = new Recommender();
		System.out.println("Loading the ratings..");
		yelpRecommender.loadRatings("/Users/Tyson/Documents/workspace/yelp_academic_dataset_review.json");
		//System.out.println("NberUser : " + yelpRecommender.ratings.size());

		for (String user : yelpRecommender.ratings.keySet())
		{
			yelpRecommender.compareToAllUsers(user);
		}
	}
}
