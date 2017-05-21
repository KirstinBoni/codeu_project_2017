// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package codeu.chat.server;

import java.util.Collection;

import codeu.chat.util.store.Store;
import codeu.chat.util.store.StoreAccessor;
import codeu.chat.common.BasicController;
import codeu.chat.common.Conversation;
import codeu.chat.common.Message;
import codeu.chat.common.RawController;
import codeu.chat.common.Time;
import codeu.chat.common.User;
import codeu.chat.common.Uuid;
import codeu.chat.common.Uuids;
import codeu.chat.util.Logger;

public final class Controller implements RawController, BasicController {

  private final static Logger.Log LOG = Logger.newLog(Controller.class);

  private final Model model;
  private final Uuid.Generator uuidGenerator;

  public Controller(Uuid serverId, Model model) {
    this.model = model;
    this.uuidGenerator = new RandomUuidGenerator(serverId, System.currentTimeMillis());
  }

  @Override
  public Message newMessage(Uuid author, Uuid conversation, String body) {
    return newMessage(createId(), author, conversation, body, Time.now());
  }

  @Override
  public User newUser(String name) {
    return newUser(createId(), name, Time.now());
  }

  @Override
  public Conversation newConversation(String title, Uuid owner) {
    return newConversation(createId(), title, owner, Time.now());
  }

  @Override
  public Message newMessage(Uuid id, Uuid author, Uuid conversation, String body, Time creationTime) {

    final User foundUser = model.userById().first(author);
    final Conversation foundConversation = model.conversationById().first(conversation);

    Message message = null;

    if (foundUser != null && foundConversation != null && isIdFree(id)) {

      message = new Message(id, Uuids.NULL, Uuids.NULL, creationTime, author, body);
      model.add(message);
      LOG.info("Message added: %s", message.id);

      // Find and update the previous "last" message so that it's "next" value
      // will point to the new message.

      if (Uuids.equals(foundConversation.lastMessage, Uuids.NULL)) {

        // The conversation has no messages in it, that's why the last message is NULL (the first
        // message should be NULL too. Since there is no last message, then it is not possible
        // to update the last message's "next" value.

      } else {
        final Message lastMessage = model.messageById().first(foundConversation.lastMessage);
        lastMessage.next = message.id;
      }

      // If the first message points to NULL it means that the conversation was empty and that
      // the first message should be set to the new message. Otherwise the message should
      // not change.

      foundConversation.firstMessage =
          Uuids.equals(foundConversation.firstMessage, Uuids.NULL) ?
          message.id :
          foundConversation.firstMessage;

      // Update the conversation to point to the new last message as it has changed.

      foundConversation.lastMessage = message.id;

      if (!foundConversation.users.contains(foundUser)) {
        foundConversation.users.add(foundUser.id);
      }
    }

    return message;
  }

  @Override
  public User newUser(Uuid id, String name, Time creationTime) {

    User user = null;

    // Check that id is free and not in use, if it is, display error
    if (isIdFree(id)) {
    	
      // Check that user name is free and not in use, if it is, display error
      if(isUserFree(name)){
	      user = new User(id, name, creationTime);
	      model.add(user);
	
	      LOG.info(
	          "newUser success (user.id=%s user.name=%s user.time=%s)",
	          id,
	          name,
	          creationTime);
      }else{
	      LOG.info(
	              "newUser fail - name in use (user.id=%s user.name=%s user.time=%s)",
	              id,
	              name,
	              creationTime);  
      }

    } else {

      LOG.info(
          "newUser fail - id in use (user.id=%s user.name=%s user.time=%s)",
          id,
          name,
          creationTime);
    }

    return user;
  }

  @Override
  public Conversation newConversation(Uuid id, String title, Uuid owner, Time creationTime) {

    final User foundOwner = model.userById().first(owner);

    Conversation conversation = null;

    // Check if id and conversation title are not in use, if they are, report error
    if (foundOwner != null && isIdFree(id) && isConversationFree(title)) {
      conversation = new Conversation(id, owner, creationTime, title);
      model.add(conversation);

      LOG.info("Conversation added: " + conversation.id);
    }else{
    	LOG.info("newConversation fail - title in use (conversation.id=%s conversation.title=%s, conversation.owner=%s, conversation.time=%s)",
    			  id, title, owner, creationTime);
    }

    return conversation;
  }
  
  @Override
  public void deleteUser(String name){
	 final User removeUser = model.userByText().first(name);
	 
	  if(removeUser != null){
		  model.userByText().removeByValue(removeUser);
		  model.userById().removeByValue(removeUser);
		  model.userByTime().removeByValue(removeUser);
		  
		  if(isUserFree(name) && isIdFree(removeUser.id)){
		      LOG.info(
			          "deleteUser success (user.id=%s user.name=%s user.time=%s)",
			          removeUser.id,
			          removeUser.name,
			          removeUser.creation);
		  }else{
		      LOG.info(
			          "deleteUser fail (user.id=%s user.name=%s user.time=%s)",
			          removeUser.id,
			          removeUser.name,
			          removeUser.creation);		  
		  }
	  }else{
		  LOG.info("deleteUser fail - user does not exist.");

	  }
  }

  private Uuid createId() {

    Uuid candidate;

    for (candidate = uuidGenerator.make();
         isIdInUse(candidate);
         candidate = uuidGenerator.make()) {

     // Assuming that "randomUuid" is actually well implemented, this
     // loop should never be needed, but just incase make sure that the
     // Uuid is not actually in use before returning it.

    }

    return candidate;
  }
 
  private boolean isConversationInUse(String conversation) {
	    return model.conversationByText().first(conversation) != null;
  }

  private boolean isConversationFree(String conversation) { return !isConversationInUse(conversation); }

  private boolean isUserInUse(String name) {
	    return model.userByText().first(name) != null;
  }

  private boolean isUserFree(String name) { return !isUserInUse(name); }
  
  private boolean isIdInUse(Uuid id) {
    return model.messageById().first(id) != null ||
           model.conversationById().first(id) != null ||
           model.userById().first(id) != null;
  }

  private boolean isIdFree(Uuid id) { return !isIdInUse(id); }

}
