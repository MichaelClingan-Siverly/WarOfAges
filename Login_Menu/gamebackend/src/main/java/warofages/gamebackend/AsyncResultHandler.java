package warofages.gamebackend;

import org.json.JSONArray;

/**
 * Created by mike on 6/6/2017.
 */

public interface AsyncResultHandler {
    void handlePollResult(JSONArray result);
}
