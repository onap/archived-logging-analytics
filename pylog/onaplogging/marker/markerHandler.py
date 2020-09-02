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

from logging import LogRecord
from logging.handlers import SMTPHandler
from typing import Tuple, List, Optional, Union

from onaplogging.utils.system import is_above_python_2_7, is_above_python_3_2

from .marker import match_markers, Marker


class MarkerNotifyHandler(SMTPHandler):
    """Handler for email notification.

    Wraps logging.handler.SMTPHandler and extends it by sending only such
    notifications which contain certain markers.

    Extends:
        SMTPHandler
    Property:
        markers: A marker or a list of markers.
    Args:
        mailhost: A (host, port) tuple.
        fromaddr: The sender of the email notification.
        toaddrs: Email notification recepient(s).
        subject: Email subject.
        credentials: A (username, password) tuple.
        secure: For example (TLS). It is used when the
                                  credentials are supplied.
        timout: Default is 5.0 seconds. Python version 3.2+
        markers: A marker or a list of markers.
    """

    @property
    def markers(self):
        # type: () -> Union[Marker, List[Marker]]
        return self._markers

    @markers.setter
    def markers(self, value):
        # type: ( Union[Marker, List[Marker]] ) - None
        self._markers = value

    def __init__(self,
                 mailhost,          # type: Tuple
                 fromaddr,          # type: str
                 toaddrs,           # type: Union[List[str], str]
                 subject,           # type: Tuple
                 credentials=None,  # type: Tuple
                 secure=None,       # type: Optional[Tuple]
                 timeout=5.0,       # type: Optional[float]
                 markers=None     # type: Optional[Union[Marker, List[Marker]]]
                 ):

        if is_above_python_3_2():
            super(MarkerNotifyHandler, self). \
            __init__(  # noqa: E122
                mailhost,
                fromaddr,
                toaddrs,
                subject,
                credentials,
                secure,
                timeout)

        elif is_above_python_2_7():
            super(MarkerNotifyHandler, self). \
            __init__(  # noqa: E122
                mailhost,
                fromaddr,
                toaddrs,
                subject,
                credentials,
                secure)

        else:
            SMTPHandler.__init__(self,
                                 mailhost,
                                 fromaddr,
                                 toaddrs,
                                 subject,
                                 credentials,
                                 secure)

        self.markers = markers

    def handle(self, record):
        # type: (LogRecord) -> bool
        """
        Handle a LogRecord record. Send an email notification.
        """
        return self.send_notification(record)

    def send_notification(self, record):
        # type: (LogRecord) -> bool
        """Email notification handler.

        Matches the record with the specific markers set for email
        notifications. Sends an email notification if that marker(s) matched.

        Args:
            record (LogRecord): A record that might contain a marker.
        Returns:
            bool: Whether a record was passed for emission (to be sent).
        """

        if  hasattr(self, "markers") and \
            self.markers is None:
            return False

        if match_markers(record, self.markers):

            if is_above_python_2_7():
                return super(MarkerNotifyHandler, self).handle(record)
            return SMTPHandler.handle(self, record)

        return False
