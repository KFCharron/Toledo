package com.mediacrossing.campaignbooks.campaigns;

import com.mediacrossing.campaignbooks.Campaign;
import java.util.List;

public interface CampaignRepository {

    List<Float> findBy(List<String> campaignIds);

}
