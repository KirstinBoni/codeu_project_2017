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

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;
import codeu.chat.client.ClientUser;
import codeu.chat.client.View;
import codeu.chat.server.Controller;
import codeu.chat.common.Conversation;
import codeu.chat.common.Message;
import codeu.chat.common.User;
import codeu.chat.common.Uuids;
import codeu.chat.common.Uuid;
import codeu.chat.common.LinearUuidGenerator;

public final class DeleteConversationTest {

  private Model model;
  private Controller controller;
  private Uuid.Generator UuidGenerator;

  @Before
  public void doBefore() {
    model = new Model();
    controller = new Controller(Uuids.NULL, model);
    UuidGenerator = new LinearUuidGenerator(null, 1, Integer.MAX_VALUE);
  }
  
  
  @Test
  public void testdeleteConversationByText() {
	  final User user = controller.newUser("user");
	  final Conversation conversation = controller.newConversation("conversation", user.id);
	  
	  controller.deleteConversation("conversation");
	  
	  assertNull(model.conversationByText().first("conversation"));
  }
  
  @Test
  public void testdeleteConversationById() {
	  final User user = controller.newUser("user");
	  final Conversation conversation = controller.newConversation("conversation", user.id);
	  
	  controller.deleteConversation("conversation");
	  
	  assertNull(model.conversationById().first(conversation.id));
  }
  
  @Test
  public void testdeleteConversationByTime() {
	  final User user = controller.newUser("user");
	  final Conversation conversation = controller.newConversation("conversation", user.id);
	  
	  controller.deleteConversation("conversation");
	  
	  assertNull(model.conversationByTime().first(conversation.creation));
  }
  
  @Test
  public void testdeleteMultipleConversations() {
	  final User testUser1 = controller.newUser("testUser1");
	  final User testUser2 = controller.newUser("testUser2");
	  final Conversation testConversation1 = controller.newConversation("testConversation1", testUser1.id);
	  final Conversation testConversation2 = controller.newConversation("testConversation2", testUser2.id);

	  controller.deleteConversation("testConversation1");
	  controller.deleteConversation("testConversation2");
	  
	  assertNull(model.conversationById().first(testConversation1.id));
	  assertNull(model.conversationById().first(testConversation2.id));
  }
  
  @Test
  public void testdeleteConversationDoesNotExistEmpty() {
	  
	  controller.deleteConversation("conversation");
	  
	  assertNull(model.conversationByText().first("conversation"));
  }
  
  @Test
  public void testdeleteConversationDoesNotExistNonEmpty() {
	  final User user = controller.newUser("user");
	  final Conversation conversation = controller.newConversation("conversation", user.id);
	  
	  controller.deleteConversation("convo");
	  
	  assertNotNull(model.conversationByText().first("conversation"));
  }
  
  @Test
  public void testdeleteConversationDoesExistNonEmpty() {
	  final User user = controller.newUser("user");
	  final Conversation conversation = controller.newConversation("conversation", user.id);

	  final User user2 = controller.newUser("user2");
	  final Conversation conversation2 = controller.newConversation("conversation2", user.id);
	  
	  controller.deleteConversation("conversation2");
	  
	  assertNull(model.conversationByText().first("conversation2"));
	  assertNotNull(model.conversationByText().first("conversation"));
  }
  
}
