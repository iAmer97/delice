package android.example.instagramclone.Model;

import java.util.List;
import java.util.Map;

public class Post {
    private String postid;
    private List postimages;
    private String description;
    private String name;
    private String publisher;
    private Map<String,Object> tags;
    private Map<String,Object> ingredients;
    private Map<String,Object> steps;
    private String numberOfServings;

    public Post(String postid, List postimages, String description, String name, String publisher, Map<String, Object> tags, Map<String, Object> ingredients, Map<String, Object> steps, String numberOfServings) {
        this.postid = postid;
        this.postimages = postimages;
        this.description = description;
        this.name = name;
        this.publisher = publisher;
        this.tags = tags;
        this.ingredients = ingredients;
        this.steps = steps;
        this.numberOfServings = numberOfServings;
    }

    public Post() {
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public List getPostimages() {
        return postimages;
    }

    public void setPostimages(List postimages) {
        this.postimages = postimages;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Map<String, Object> getTags() {
        return tags;
    }

    public void setTags(Map<String, Object> tags) {
        this.tags = tags;
    }

    public Map<String, Object> getIngredients() {
        return ingredients;
    }

    public void setIngredients(Map<String, Object> ingredients) {
        this.ingredients = ingredients;
    }

    public Map<String, Object> getSteps() {
        return steps;
    }

    public void setSteps(Map<String, Object> steps) {
        this.steps = steps;
    }

    public String getNumberOfServings() {
        return numberOfServings;
    }

    public void setNumberOfServings(String numberOfServings) {
        this.numberOfServings = numberOfServings;
    }
}
