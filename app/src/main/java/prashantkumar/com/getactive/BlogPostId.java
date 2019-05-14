package prashantkumar.com.getactive;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;

/**
 * Created by Prashant on 14-05-2019.
 */

public class BlogPostId {

    @Exclude
    public  String BlogPostId;

    public  < T extends BlogPostId> T withId(@NonNull final String id){
        this.BlogPostId = id;
        return (T) this;
    }
}
