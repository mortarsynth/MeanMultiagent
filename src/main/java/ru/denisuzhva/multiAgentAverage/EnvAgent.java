package main.java.ru.denisuzhva.multiAgentAverage;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
//import jade.tools.sniffer.Message;

import java.util.*;



public class EnvAgent extends Agent {
	private String selfId;
    private String consensusStateConvId;
	

	@Override
	protected void setup() {
		consensusStateConvId = "consensus-state";
		selfId = getAID().getLocalName();
        System.out.println("Agent " + selfId + " is ready");

		addBehaviour(new StateTranslator());
		addBehaviour(new PoolTranslator());
	}


	@Override
    protected void takeDown() {
        System.out.println("Agent " + selfId + " is terminating");
    }


	private class StateTranslator extends CyclicBehaviour {

		private float inState;
		private float outState;

		public void action() {
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE), 
					MessageTemplate.MatchConversationId(consensusStateConvId));
			ACLMessage stateMsg = myAgent.receive(mt);
			if (stateMsg != null) {
				String msgContent = stateMsg.getContent();
				String[] contentList = msgContent.split(" ");
				inState = Float.parseFloat(contentList[0]);

				// do noizy stuff here
				outState = inState;

				ACLMessage stateFurther = new ACLMessage(ACLMessage.PROPAGATE);
				stateFurther.setContent(String.valueOf(outState));
				stateFurther.setConversationId(consensusStateConvId);
				stateFurther.setReplyWith(stateMsg.getReplyWith());
				stateFurther.setSender(stateMsg.getSender());
				for (int i = 1; i < contentList.length; i++) {
					String contentEnt = contentList[i];
					stateFurther.addReceiver(new AID(contentEnt, AID.ISLOCALNAME));
				}
				myAgent.send(stateFurther);
			}
		}
	}


	private class PoolTranslator extends CyclicBehaviour {

		private float inPool;
		private float outPoolDist;
		private int numNeigh;

		public void action() {
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE), 
					MessageTemplate.MatchConversationId(consensusStateConvId));
			ACLMessage poolMsg = myAgent.receive(mt);
			if (poolMsg != null) {
				String msgContent = poolMsg.getContent();
				String[] contentList = msgContent.split(" ");
				inPool = Float.parseFloat(contentList[0]);
				numNeigh = contentList.length - 1;

				// do noizy stuff here
				outPoolDist = inPool / numNeigh;

				ACLMessage poolFurther = new ACLMessage(ACLMessage.PROPOSE);
				poolFurther.setContent(String.valueOf(outPoolDist));
				poolFurther.setConversationId(consensusStateConvId);
				poolFurther.setReplyWith(poolMsg.getReplyWith());
				poolFurther.setSender(poolMsg.getSender());
				for (int i = 1; i < contentList.length; i++) {
					String contentEnt = contentList[i];
					poolFurther.addReceiver(new AID(contentEnt, AID.ISLOCALNAME));
				}
				myAgent.send(poolFurther);
			}
		}
	}
}
