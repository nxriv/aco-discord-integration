package drm.bot.utils;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.attribute.IGuildChannelContainer;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class MongoDBHandler {
    private final MongoCollection<Document> collection;
    private final MongoCollection<Document> statisticsCollection;
    private static final int ENTRIES_PER_PAGE = 10;
    private JDA jda; // JDA instance

    public MongoDBHandler(String connectionString, String dbName, String collectionName, String statisticsCollectionName) {
        var mongoClient = MongoClients.create(connectionString);
        MongoDatabase database = mongoClient.getDatabase(dbName);
        collection = database.getCollection(collectionName);
        statisticsCollection = database.getCollection(statisticsCollectionName);
    }

    public UserStatistics getUserStatistics(String userId) {
        Document statsDoc = statisticsCollection.find(Filters.eq("userId", userId)).first();
        UserStatistics stats = new UserStatistics();

        if (statsDoc != null) {
            stats.setNumCheckouts(statsDoc.getInteger("numCheckouts", 0));

            Double totalValue = statsDoc.getDouble("totalValue");
            if (totalValue == null) {
                totalValue = 0.0;
            }
            stats.setTotalValue(totalValue);

            Long numAccounts = statsDoc.getLong("numAccounts");
            if (numAccounts == null) {
                numAccounts = 0L;
            }
            stats.setNumAccounts(statsDoc.getLong("numAccounts"));
        }

        return stats;
    }

    public void updateUserStatistics(String userId, int numCheckouts, double totalValue) {
        Document filter = new Document("userId", userId);
        Document update = new Document("$set", new Document("numCheckouts", numCheckouts)
                .append("totalValue", totalValue));
        statisticsCollection.updateOne(filter, update);
    }

    public List<Document> getPageEntries(int page) {
        int skip = (page - 1) * ENTRIES_PER_PAGE; // Calculate the number of documents to skip
        List<Document> entries = new ArrayList<>();

        // Perform the query with skip and limit
        try (MongoCursor<Document> cursor = collection.find()
                .skip(skip)
                .limit(ENTRIES_PER_PAGE)
                .iterator()) {
            while (cursor.hasNext()) {
                entries.add(cursor.next());
            }
        } catch (Exception e) {
            e.printStackTrace(); // Handle exceptions as appropriate
        }

        return entries;
    }


    public static class UserStatistics {
        private long numCheckouts;
        private double totalValue;
        private long numAccounts;

        // Getters and setters for the fields
        public long getNumCheckouts() {
            return numCheckouts;
        }

        public void setNumCheckouts(long numCheckouts) {
            this.numCheckouts = numCheckouts;
        }

        public double getTotalValue() {
            return totalValue;
        }

        public void setTotalValue(double totalValue) {
            this.totalValue = totalValue;
        }

        public long getNumAccounts() {
            return numAccounts;
        }

        public void setNumAccounts(long numAccounts) {
            this.numAccounts = numAccounts;
        }
    }

    public void addEmail(String userId, String email) {
        Document doc = new Document("userId", userId)
                .append("email", email);
        collection.insertOne(doc);
    }

    public boolean removeEmail(String userId, String email) {
        var result = collection.deleteOne(Filters.and(
                Filters.eq("userId", userId),
                Filters.eq("email", email)));
        return result.getDeletedCount() > 0;
    }

    // Remove an email by email address regardless of user ID (admin-only)
    public boolean removeEmailByEmail(String email) {
        var result = collection.deleteMany(Filters.eq("email", email));
        return result.getDeletedCount() > 0;
    }

    // Check if an email already exists for a user's ID
    public boolean isEmailExists(String userId, String email) {
        Document query = new Document("userId", userId).append("email", email);
        return collection.countDocuments(query) > 0;
    }

    // Get the user ID associated with an email
    public String getUserIdByEmail(String email) {
        try {
            Document query = new Document("email", email);
            try (MongoCursor<Document> cursor = collection.find(query).iterator()) {

                if (cursor.hasNext()) {
                    Document document = cursor.next();
                    return document.getString("userId");
                }
            }
        } catch (Exception e) {
            // Handle any exceptions here
            e.printStackTrace();
        }

        return null; // Return null if the email is not found
    }

    public void addChannelListener(String channelId) {
        Document doc = new Document("channelId", channelId);
        collection.insertOne(doc);
    }

    public void removeChannelListener(String channelId) {
        collection.deleteOne(new Document("channelId", channelId));
    }

    public List<String> getChannelListeners() {
        List<String> channels = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                channels.add(doc.getString("channelId"));
            }
        }
        return channels;
    }
    public List<String> getAllEmails() {
        List<String> emails = new ArrayList<>();
        collection.find().forEach(document -> {
            String userId = document.getString("userId");
            String email = document.getString("email");
            String userMention = Wrapper.getJDA().getUserById(userId) != null ? Wrapper.getJDA().getUserById(userId).getAsMention() : "User not found";
            emails.add(email + " - UserID: " + userId + " (" + userMention + ")");
        });
        return emails;
    }

    public List<String> getUserEmails(String userId) {
        List<String> emails = new ArrayList<>();
        collection.find(Filters.eq("userId", userId))
                .forEach(document -> emails.add(document.getString("email")));
        return emails;
    }

    public void setHideEmail(String userId, boolean hideEmail) {
        Document filter = new Document("userId", userId);
        Document update = new Document("$set", new Document("hideEmail", hideEmail));
        collection.updateOne(filter, update);
    }

    // Method to get hideEmail status
    public boolean getHideEmail(String userId) {
        Document query = new Document("userId", userId);
        Document user = collection.find(query).first();
        return user != null && user.getBoolean("hideEmail", false);
    }

    public void setNotifyPreference(String userId, boolean notify) {
        Document filter = new Document("userId", userId);
        Document update = new Document("$set", new Document("notify", notify));
        collection.updateOne(filter, update, new UpdateOptions().upsert(true));
    }

    public boolean getNotifyPreference(String userId) {
        Document query = new Document("userId", userId);
        Document userDoc = collection.find(query).first();
        if (userDoc != null && userDoc.containsKey("notify")) {
            return userDoc.getBoolean("notify", true); // Default to true if not explicitly set
        }
        return true; // Default to true if user not found
    }



    // Method to list all users with their hideEmail status (for admin)
    public List<Document> getAllUsersHideEmail() {
        List<Document> users = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                users.add(cursor.next());
            }
        }
        return users;
    }

    public void updateStatistics(String userId, double originalPrice) {
        // Find the statistics document for the user
        Document stats = statisticsCollection.find(Filters.eq("userId", userId)).first();

        // Count the number of email addresses associated with the user ID
        long numAccounts = collection.countDocuments(Filters.eq("userId", userId));

        if (stats == null) {
            // If the user does not have a stats document, create one
            stats = new Document("userId", userId)
                    .append("numCheckouts", 1)
                    .append("totalValue", originalPrice)
                    .append("numAccounts", numAccounts);
            statisticsCollection.insertOne(stats);
        } else {
            // Increment the number of checkouts and add to the total value
            statisticsCollection.updateOne(
                    Filters.eq("userId", userId),
                    Updates.combine(
                            Updates.inc("numCheckouts", 1),
                            Updates.inc("totalValue", originalPrice),
                            Updates.set("numAccounts", numAccounts) // Update the number of accounts
                    )
            );
        }
    }

    public void incrementAccountCount(String userId) {
        statisticsCollection.updateOne(Filters.eq("userId", userId), Updates.inc("numAccounts", 1));
    }

    public void decrementAccountCount(String userId) {
        statisticsCollection.updateOne(Filters.eq("userId", userId), Updates.inc("numAccounts", -1));
    }

    public Document getStatistics(String userId) {
        return statisticsCollection.find(Filters.eq("userId", userId)).first();
    }

    public void setJDA(JDA jda) {
        this.jda = jda;
    }
}

