package com.cognia.api.domain.service;

import com.cognia.api.domain.model.*;
import java.util.*;

public class LearningPathGenerator {
	public List<PathStep> generate(List<Concept> concepts, List<Prerequisite> graph, Map<UUID, Double> mastery, double threshold) {
		if (!Double.isFinite(threshold) || threshold < 0 || threshold > 1) throw new IllegalArgumentException("Invalid mastery threshold");
		var byId = new HashMap<UUID, Concept>(); concepts.stream().filter(Concept::active).forEach(c -> byId.put(c.id(), c));
		var incoming = new HashMap<UUID, Integer>(); var dependents = new HashMap<UUID, List<UUID>>();
		byId.keySet().forEach(id -> incoming.put(id, 0));
		for (var edge : graph) if (byId.containsKey(edge.conceptId()) && byId.containsKey(edge.prerequisiteId())) { incoming.compute(edge.conceptId(), (k,v)->v+1); dependents.computeIfAbsent(edge.prerequisiteId(), k->new ArrayList<>()).add(edge.conceptId()); }
		var available = new PriorityQueue<Concept>(Comparator.comparingInt(Concept::order).thenComparing(c->c.id().toString()));
		incoming.forEach((id,n)->{if(n==0)available.add(byId.get(id));}); var sorted = new ArrayList<Concept>();
		while(!available.isEmpty()){var c=available.remove();sorted.add(c);for(var d:dependents.getOrDefault(c.id(),List.of()))if(incoming.compute(d,(k,v)->v-1)==0)available.add(byId.get(d));}
		if(sorted.size()!=byId.size())throw new IllegalArgumentException("Prerequisite graph contains a cycle");
		var steps=new ArrayList<PathStep>(); int order=0;
		for(var c:sorted){var prerequisites=graph.stream().filter(e->e.conceptId().equals(c.id())).map(Prerequisite::prerequisiteId).filter(byId::containsKey).toList();var prerequisitesMastered=prerequisites.stream().allMatch(id->steps.stream().anyMatch(p->p.conceptId().equals(id)&&p.state()==PathStep.State.DOMINADO));var hasMastery=mastery.containsKey(c.id())&&mastery.get(c.id())>=threshold;
			var state=prerequisitesMastered?(hasMastery?PathStep.State.DOMINADO:PathStep.State.DISPONIBLE):PathStep.State.BLOQUEADO;
			steps.add(new PathStep(null,c.id(),order++,state));}
		return List.copyOf(steps);
	}
	public List<PathStep> regenerate(List<Concept> concepts,List<Prerequisite> graph,Map<UUID,Double> mastery,double threshold,List<PathStep> previous){var effective=new HashMap<>(mastery);previous.stream().filter(s->s.state()==PathStep.State.DOMINADO).forEach(s->effective.merge(s.conceptId(),1d,Math::max));var generated=generate(concepts,graph,effective,threshold);return unlockEligible(generated,graph,effective,threshold);}
	public List<PathStep> afterResult(List<PathStep> source, UUID conceptId, boolean passed, List<Prerequisite> graph) {
		var steps=new ArrayList<>(source); int index=index(steps,conceptId); var target=steps.get(index);
		if(target.state()==PathStep.State.DISPONIBLE || target.state()==PathStep.State.REFUERZO) target=target.transitionTo(PathStep.State.EN_PROGRESO);
		steps.set(index,target.transitionTo(passed?PathStep.State.DOMINADO:PathStep.State.REFUERZO));
		for(int i=0;i<steps.size();i++){var step=steps.get(i);var prereqs=graph.stream().filter(e->e.conceptId().equals(step.conceptId())).map(Prerequisite::prerequisiteId).filter(id->steps.stream().anyMatch(p->p.conceptId().equals(id))).toList();if(step.state()==PathStep.State.BLOQUEADO&&!prereqs.isEmpty()&&prereqs.stream().allMatch(id->steps.stream().anyMatch(p->p.conceptId().equals(id)&&p.state()==PathStep.State.DOMINADO)))steps.set(i,step.transitionTo(PathStep.State.DISPONIBLE));}
		return List.copyOf(steps);
	}
	public List<PathStep> unlockEligible(List<PathStep> source,List<Prerequisite> graph,Map<UUID,Double> mastery,double threshold){var steps=new ArrayList<>(source);boolean changed;do{changed=false;for(int i=0;i<steps.size();i++){var step=steps.get(i);if(step.state()!=PathStep.State.BLOQUEADO&&step.state()!=PathStep.State.DISPONIBLE)continue;var prereqs=graph.stream().filter(e->e.conceptId().equals(step.conceptId())).map(Prerequisite::prerequisiteId).filter(id->steps.stream().anyMatch(p->p.conceptId().equals(id))).toList();if(!prereqs.isEmpty()&&prereqs.stream().allMatch(id->steps.stream().anyMatch(p->p.conceptId().equals(id)&&p.state()==PathStep.State.DOMINADO))&&mastery.containsKey(step.conceptId())&&mastery.get(step.conceptId())>=threshold){var unlocked=step.state()==PathStep.State.BLOQUEADO?step.transitionTo(PathStep.State.DISPONIBLE):step;steps.set(i,unlocked.transitionTo(PathStep.State.DOMINADO));changed=true;}}}while(changed);return List.copyOf(steps);}
	private static int index(List<PathStep> steps,UUID concept){for(int i=0;i<steps.size();i++)if(steps.get(i).conceptId().equals(concept))return i;throw new IllegalArgumentException("Concept is not on path");}
}
