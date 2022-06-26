package it.polito.tdp.poweroutages.model;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.polito.tdp.poweroutages.DAO.PowerOutageDAO;

public class Model {
	
	PowerOutageDAO podao;
	private List<PowerOutages> migliore;
	private List<PowerOutages> poList;
	private int maxAffected;
	
	
	public Model() {
		podao = new PowerOutageDAO();
	}
	
	public List<Nerc> getNercList() {
		return podao.getNercList();
	}
	
	public List<PowerOutages> getWorstCase(Nerc nerc, int maxYears, int maxHours) {
		//ripristino soluzione migliore
		this.migliore = new ArrayList<PowerOutages>();
		List<PowerOutages> parziale = new ArrayList<PowerOutages>();
		this.maxAffected = 0;
		
		//ottengo la lista dei P.O. dato il nerc
		this.poList = podao.getPowerOutagesByNerc(nerc);
		Collections.sort(poList);
		
		cerca(parziale, maxYears, maxHours);
		
		return migliore;
	}
	
	private void cerca(List<PowerOutages> parziale, int maxYears, int maxHours) {
		if(countAffected(parziale) > maxAffected) {
			maxAffected = countAffected(parziale);
			migliore = new ArrayList<PowerOutages>(parziale);
		}
		
		for(PowerOutages po : poList) {
			if(!parziale.contains(po)) {
				parziale.add(po);
				//verifico che l'aggiunta crei una soluzione parziale corretta
				//e richiamo la ricorsione con la nuova parziale
				if(countHours(parziale)<=maxHours && countYears2(parziale)<=maxYears) {
					cerca(parziale, maxYears, maxHours);
				}
				
				parziale.remove(po);
			}
		}
		
	}

	public int countAffected(List<PowerOutages> parziale) {
		int numAffected = 0;
		for(PowerOutages po : parziale) {
			numAffected += po.getCustomersAffected();
		}
		
		return numAffected;
	}
	
//	private int countYears(List<PowerOutages> parziale) {
//		return parziale.get(parziale.size()-1).getDateEventBegan().getYear() - parziale.get(0).getDateEventBegan().getYear();
//	}
	
	private int countYears2(List<PowerOutages> parziale) {
		int minYear = 10000;
		int maxYear = 0;
		int diffYears;
		
		for(PowerOutages po : parziale) {
			if(po.getDateEventBegan().getYear()<minYear)
				minYear = po.getDateEventBegan().getYear();
			
			if(po.getDateEventBegan().getYear()>maxYear)
				maxYear = po.getDateEventBegan().getYear();
		}
		diffYears = maxYear - minYear;
		return diffYears;
	}
	
	public long countHours(List<PowerOutages> parziale) {
		long poHours = 0;
		long totHours = 0;
		for(PowerOutages po : parziale) {
//			poHours = Duration.between(po.getDateEventBegan(), po.getDateEventFinished()).toHours();
			poHours = po.getDateEventBegan().until(po.getDateEventFinished(), ChronoUnit.HOURS);
			totHours += poHours;
		}
		
		return totHours;
	}

}
