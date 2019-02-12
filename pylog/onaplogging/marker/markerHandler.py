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
from .import matchMarkerHelp


class MarkerNotifyHandler(SMTPHandler):

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

        if self.markers is None:
            return False

        if matchMarkerHelp(record, self.markers):
            if sys.version_info > (2, 7):
                return super(SMTPHandler, self).handle(record)
            else:
                return SMTPHandler.handle(self, record)

        return False
