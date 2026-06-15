package com.medsupply.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.medsupply.models.Intervention;
import com.medsupply.utils.SupabaseClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 * InterventionDAO - Data Access Object for Intervention table
 * Handles all database operations for interventions
 */
public class InterventionDAO {

    /**
     * Find intervention by ID
     * @param interventionId Intervention ID (UUID)
     * @return Intervention object if found, null otherwise
     */
    public Intervention findById(String interventionId) throws Exception {
        String filters = "id=eq." + SupabaseClient.enc(interventionId);
        String response = SupabaseClient.get("maintenance_interventions", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToIntervention(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Find interventions by request ID
     * @param requestId Request ID
     * @return List of interventions for the request
     */
    public List<Intervention> findByRequestId(String requestId) throws Exception {
        String filters = "contract_id=eq." + SupabaseClient.enc(requestId) + "&order=intervention_date.desc";
        String response = SupabaseClient.get("maintenance_interventions", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<Intervention> interventions = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            interventions.add(mapJsonToIntervention(jsonArray.get(i).getAsJsonObject()));
        }
        return interventions;
    }

    /**
     * Find interventions by completion status
     * @param completionStatus Completion status
     * @return List of interventions with the status
     */
    public List<Intervention> findByCompletionStatus(String completionStatus) throws Exception {
        String filters = "completion_status=eq." + SupabaseClient.enc(completionStatus) + "&order=intervention_date.desc";
        String response = SupabaseClient.get("maintenance_interventions", filters);
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<Intervention> interventions = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            interventions.add(mapJsonToIntervention(jsonArray.get(i).getAsJsonObject()));
        }
        return interventions;
    }

    /**
     * Find all interventions
     * @return List of all interventions
     */
    public List<Intervention> findAll() throws Exception {
        String response = SupabaseClient.get("maintenance_interventions", "order=intervention_date.desc");
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        List<Intervention> interventions = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            interventions.add(mapJsonToIntervention(jsonArray.get(i).getAsJsonObject()));
        }
        return interventions;
    }

    /**
     * Create a new intervention
     * @param intervention Intervention object to create
     * @return Created intervention
     */
    public Intervention create(Intervention intervention) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("id", intervention.getInterventionId());
        body.addProperty("contract_id", intervention.getRequestId());
        body.addProperty("technician_name", intervention.getTechnicianName());
        body.addProperty("intervention_date", intervention.getInterventionDate().toString());
        body.addProperty("diagnosis", intervention.getDiagnosis());
        body.addProperty("actions_performed", intervention.getActionsPerformed());
        body.addProperty("replaced_parts", intervention.getReplacedParts());
        body.addProperty("intervention_cost", intervention.getInterventionCost());
        body.addProperty("completion_status", intervention.getCompletionStatus());
        body.addProperty("notes", intervention.getNotes());
        
        LocalDateTime now = LocalDateTime.now();
        body.addProperty("created_at", now.toString());
        body.addProperty("updated_at", now.toString());
        
        String response = SupabaseClient.post("maintenance_interventions", body.toString());
        
        JsonArray jsonArray = SupabaseClient.parseJsonArray(response);
        if (jsonArray.size() > 0) {
            return mapJsonToIntervention(jsonArray.get(0).getAsJsonObject());
        }
        return null;
    }

    /**
     * Update intervention completion status
     * @param interventionId Intervention ID
     * @param completionStatus New completion status
     * @return true if update successful
     */
    public boolean updateCompletionStatus(String interventionId, String completionStatus) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("completion_status", completionStatus);
        body.addProperty("updated_at", LocalDateTime.now().toString());
        
        String filters = "id=eq." + SupabaseClient.enc(interventionId);
        String response = SupabaseClient.patchWithFilters("maintenance_interventions", filters, body.toString());
        return SupabaseClient.affectedRows(response) > 0;
    }

    /**
     * Update intervention details
     * @param interventionId Intervention ID
     * @param actionsPerformed Actions performed
     * @param replacedParts Replaced parts
     * @param interventionCost Intervention cost
     * @param notes Notes
     * @return true if update successful
     */
    public boolean updateDetails(String interventionId, String actionsPerformed, String replacedParts, 
                                  BigDecimal interventionCost, String notes) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("actions_performed", actionsPerformed);
        body.addProperty("replaced_parts", replacedParts);
        body.addProperty("intervention_cost", interventionCost);
        body.addProperty("notes", notes);
        body.addProperty("updated_at", LocalDateTime.now().toString());
        
        String filters = "id=eq." + SupabaseClient.enc(interventionId);
        String response = SupabaseClient.patchWithFilters("maintenance_interventions", filters, body.toString());
        return SupabaseClient.affectedRows(response) > 0;
    }

    /**
     * Delete intervention
     * @param interventionId Intervention ID
     * @return true if deletion successful
     */
    public boolean delete(String interventionId) throws Exception {
        String response = SupabaseClient.delete("maintenance_interventions", interventionId);
        return SupabaseClient.affectedRows(response) > 0;
    }

    /**
     * Map JSON to Intervention object
     */
    private Intervention mapJsonToIntervention(JsonObject json) {
        Intervention intervention = new Intervention();
        if (json.has("id") && !json.get("id").isJsonNull())
            intervention.setInterventionId(json.get("id").getAsString());
        if (json.has("contract_id") && !json.get("contract_id").isJsonNull())
            intervention.setRequestId(json.get("contract_id").getAsString());
        if (json.has("technician_name") && !json.get("technician_name").isJsonNull())
            intervention.setTechnicianName(json.get("technician_name").getAsString());
        if (json.has("intervention_date") && !json.get("intervention_date").isJsonNull()) {
            try {
                String dateStr = json.get("intervention_date").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                intervention.setInterventionDate(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        if (json.has("diagnosis") && !json.get("diagnosis").isJsonNull())
            intervention.setDiagnosis(json.get("diagnosis").getAsString());
        if (json.has("actions_performed") && !json.get("actions_performed").isJsonNull())
            intervention.setActionsPerformed(json.get("actions_performed").getAsString());
        if (json.has("replaced_parts") && !json.get("replaced_parts").isJsonNull())
            intervention.setReplacedParts(json.get("replaced_parts").getAsString());
        if (json.has("intervention_cost") && !json.get("intervention_cost").isJsonNull())
            intervention.setInterventionCost(new BigDecimal(json.get("intervention_cost").getAsString()));
        if (json.has("completion_status") && !json.get("completion_status").isJsonNull())
            intervention.setCompletionStatus(json.get("completion_status").getAsString());
        if (json.has("notes") && !json.get("notes").isJsonNull())
            intervention.setNotes(json.get("notes").getAsString());
        if (json.has("created_at") && !json.get("created_at").isJsonNull()) {
            try {
                String dateStr = json.get("created_at").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                intervention.setCreatedAt(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        if (json.has("updated_at") && !json.get("updated_at").isJsonNull()) {
            try {
                String dateStr = json.get("updated_at").getAsString()
                    .replace("Z", "")
                    .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                    .replaceAll("\\.[0-9]+$", "");
                intervention.setUpdatedAt(LocalDateTime.parse(dateStr));
            } catch (Exception ignored) {}
        }
        return intervention;
    }
}
