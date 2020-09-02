# Copyright 2018 ke liang <lokyse@163.com>.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import sys
from logging.handlers import SMTPHandler
from .marker import matchMarkerHelp


class MarkerNotifyHandler(SMTPHandler):
    """Handler for email notification.

    Wraps logging.handler.SMTPHandler and extends it by sending only such
    notifications which contain certain markers.

    Extends:
        SMTPHandler

    Attributes:
        markers (Marker/list): A marker or a list of markers.

    Args:
        mailhost (tuple): A (host, port) tuple.
        fromaddr (str): The sender of the email notification.
        toaddrs (list/str): Email notification recepient(s).
        subject (str): Email subject.
        credentials (tuple): A (username, password) tuple.
        secure (tuple, optional): For example (TLS). It is used when the
                                  credentials are supplied.
        timout (float, optional): Default is 5.0 seconds.
        markers (Marker/list, optional): A marker or a list of markers.
    """

    def __init__(self, mailhost, fromaddr, toaddrs, subject,
                 credentials=None, secure=None, timeout=5.0, markers=None):

        if sys.version_info > (3, 2):
            super(MarkerNotifyHandler, self).__init__(
                mailhost, fromaddr, toaddrs, subject,
                credentials, secure, timeout)
        elif sys.version_info > (2, 7):
            super(MarkerNotifyHandler, self).__init__(
                mailhost, fromaddr, toaddrs, subject,
                credentials, secure)
        else:
            SMTPHandler.__init__(self,
                                 mailhost, fromaddr, toaddrs, subject,
                                 credentials, secure)

        self.markers = markers

    def handle(self, record):
        """Email notification handler.

        Matches the record with the specific markers set for email
        notifications. Sends an email notification if that marker(s) matched.

        Args:
            record (LogEvent): A record that might contain a marker.

        Returns:
            bool: Whether a record was passed for emission (to be sent).
        """

        if self.markers is None:
            return False

        if matchMarkerHelp(record, self.markers):
            if sys.version_info > (2, 7):
                return super(MarkerNotifyHandler, self).handle(record)
            else:
                return SMTPHandler.handle(self, record)

        return False
