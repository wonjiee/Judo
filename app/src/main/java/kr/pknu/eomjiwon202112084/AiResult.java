package kr.pknu.eomjiwon202112084;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class AiResult {
    // API 응답 필드와 일치하도록 @SerializedName 사용 (선택적이지만 명시적입니다)
    @SerializedName("summary")
    public String summary;

    @SerializedName("rating")
    public String rating;

    @SerializedName("reason")
    public String reason;

    @SerializedName("details")
    public List<String> details;

}